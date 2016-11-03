package org.unfairfunction.smartsox.things.door

import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.persistence.inmemory.extension.{ InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension }
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestKit

trait InMemoryCleanup extends BeforeAndAfterEach with BeforeAndAfterAll { _: Suite =>

  implicit def system: ActorSystem
  
  override def afterAll = {
    system.terminate()
  }


  override protected def beforeEach(): Unit = {
    val tp = TestProbe()
    tp.send(StorageExtension(system).journalStorage, InMemoryJournalStorage.ClearJournal)
    tp.expectMsg(akka.actor.Status.Success(""))
    tp.send(StorageExtension(system).snapshotStorage, InMemorySnapshotStorage.ClearSnapshots)
    tp.expectMsg(akka.actor.Status.Success(""))
    super.beforeEach()
  }
}