package ethanp.firstVersion

import akka.actor.ActorRef
import ethanp.file.{FileInfo, FileToDownload, Sha2}

/**
 * Ethan Petuchowski
 * 6/4/15
 */
case class LoadFile(pathString: String, name: String)
case class TrackerLoc(trackerPath: ActorRef)
case class ListTracker(trackerPath: ActorRef)
case class TrackerKnowledge(knowledge: List[FileToDownload])
case class InformTrackerIHave(fileInfo: FileInfo)
case class TrackerSideError(errorString: String)
case class ClientError(errorString: String)
case class PeerSideError(errorString: String)
case class SuccessfullyAdded(filename: String)
case class DownloadFile(filename: String)
case class DownloadFileFrom(trackerLoc: ActorRef, filename: String)
case class Piece(arr: Array[Byte], pieceIdx: Int)
sealed trait ChunkStatus extends Serializable
case class ChunkComplete(chunkIdx: Int) extends ChunkStatus
case class ChunkDLFailed(chunkIdx: Int, peerPath: ActorRef) extends ChunkStatus
case class ChunkRequest(infoAbbrev: Sha2, chunkIdx: Int)
case object ChunkSuccess
case class DownloadSpeed(numBytes: Int)
case class DownloadSuccess(filename: String)
case class Ping(infoAbbrev: Sha2)
