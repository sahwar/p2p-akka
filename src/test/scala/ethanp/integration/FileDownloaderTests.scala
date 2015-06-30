package ethanp.integration

import java.io.File

import akka.actor.{ActorRef, Props}
import akka.testkit.TestActorRef
import ethanp.file.FileToDownload
import ethanp.firstVersion.{Leeching, Seeding, Ping, FileDownloader}
import ethanp.integration.BaseTester.ForwardingActor

import scala.collection.immutable
import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 6/14/15
 */
class FileDownloaderTests extends BaseTester {

    /* TODO Tests are a MEANS to an END
     *      no need to focus on 'absolute robustness'
     * TODO keep tests at a higher level
     *      this will make refactoring simpler
     */

    "a FileDownloader" when {
        "there are 5 seeders and 5 leechers" when {
            val fwdActors = (1 to 10).map(i => system.actorOf(Props(classOf[ForwardingActor], self))).toSet
            val (seeders, leechers) = splitAtIndex(fwdActors, 5)
//            val ftd = FileToDownload(testTextP2P.fileInfo, seeders, leechers)

            "2 seeders and 3 leechers are down" when {
                val (liveSeeders, deadSeeders) = splitAtIndex(seeders, 3)
                val (liveLeechers, deadLeechers) = splitAtIndex(leechers, 2)
                val dlDir = new File("test_downloads")
                dlDir.deleteOnExit()

                val fileInfo = inputTextP2P.fileInfo
                val ftd = FileToDownload(fileInfo, seeders, leechers)
                val fDlRef = TestActorRef(Props(classOf[FileDownloader], ftd, dlDir))
                val fDlPtr: FileDownloader = fDlRef.underlyingActor
                "first starting up" should {
                    "check which peers are alive" in {
                        quickly {
                            expectNOf(10, Ping(fileInfo.abbreviation))
                        }
                    }
                }

                "getting chunk availabilities" should {
                    "believe seeders are seeders" in {
                        liveSeeders.foreach(fDlRef.tell(Seeding, _))
                        assert(liveSeeders forall fDlPtr.liveSeeders.contains)
                    }
                    "know avbl of leechers" in {
                        // test file has "3" chunks
                        var unavbl = new mutable.BitSet(fileInfo.numChunks)
                        for ((leecher, idx) <- liveLeechers.zipWithIndex) {
                            fDlRef.tell(Leeching((unavbl += idx).toImmutable), leecher)
                        }

                        unavbl = new mutable.BitSet(fileInfo.numChunks)
                        for ((leecher, idx) <- liveLeechers.zipWithIndex) {
                            assert(fDlPtr.liveLeechers(leecher) == (unavbl += idx).toImmutable)
                        }
                    }
                    "leave aside peers who don't respond" in {
                        fDlPtr.nonResponsiveDownloadees should have size 5
                    }
                }
            }
        }
    }
}
