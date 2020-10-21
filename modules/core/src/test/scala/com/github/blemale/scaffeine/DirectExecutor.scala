package com.github.blemale.scaffeine

import java.util.concurrent.Executor

object DirectExecutor extends Executor {
  override def execute(command: Runnable): Unit = command.run()
}
