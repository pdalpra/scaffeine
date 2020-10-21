package com.github.blemale.scaffeine

import scala.concurrent.ExecutionContext

private[scaffeine] object DirectExecutionContext extends ExecutionContext {
  override def execute(command: Runnable): Unit = command.run()

  override def reportFailure(cause: Throwable): Unit =
    throw new IllegalStateException(
      "problem in scaffeine internal callback",
      cause
    )
}
