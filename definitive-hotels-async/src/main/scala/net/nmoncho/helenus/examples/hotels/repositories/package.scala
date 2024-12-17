package net.nmoncho.helenus.examples.hotels

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

package object repositories {
  import net.nmoncho.helenus._

  def fetchAllPages[T](prev: Iterator[T], page: MappedAsyncPagingIterable[T])(
      implicit ec: ExecutionContext
  ): Future[Iterator[T]] =
    page.nextPage.flatMap {
      case Some((next, nextPage)) => fetchAllPages(prev.concat(next), nextPage)
      case None => Future.successful(prev)
    }
}
