package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.MySpecification
import com.thoughtworks.pli.intellij.remotepair.PairEvent

class FileChangeSpec extends MySpecification {

  "File related event" should {
    def checking(event: PairEvent) = new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")

      client(context1).send(event, event)

      there was two(context2).writeAndFlush(event.toMessage)
    }
    "broadcast to other contexts for CreateFileEvent" in new ProtocolMocking {
      checking(createFileEvent)
    }
    "broadcast to other contexts for DeleteFileEvent" in new ProtocolMocking {
      checking(deleteFileEvent)
    }
    "broadcast to other contexts for CreateDirEvent" in new ProtocolMocking {
      checking(createDirEvent)
    }
    "broadcast to other contexts for DeleteDirEvent" in new ProtocolMocking {
      checking(deleteDirEvent)
    }
    "broadcast to other contexts for RenameEvent" in new ProtocolMocking {
      checking(renameEvent)
    }
  }

  "SyncFilesRequest" should {
    "forward to master" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")
      client(context2).beMaster()
      client(context1).send(syncFilesRequest)
      there was one(context2).writeAndFlush(syncFilesRequest.toMessage)
      there was no(context1).writeAndFlush(syncFilesRequest.toMessage)
    }
  }


}
