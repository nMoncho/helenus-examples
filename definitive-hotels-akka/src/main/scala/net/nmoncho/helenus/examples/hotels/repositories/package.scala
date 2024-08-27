package net.nmoncho.helenus.examples.hotels

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

package object repositories {
  import net.nmoncho.helenus._

  def fetchPage[T](prev: Iterator[T], page: MappedAsyncPagingIterable[T])(
      implicit ec: ExecutionContext
  ): Future[Iterator[T]] =
    page.nextPage.flatMap { next =>
      if (page.hasMorePages()) fetchPage(prev.concat(next), page)
      else Future.successful(prev.concat(next))
    }
}
