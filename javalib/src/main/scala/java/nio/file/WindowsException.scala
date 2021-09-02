package java.nio.file

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scalanative.libc.{string, errno => stdErrno}
import scalanative.posix.errno.ENOTDIR
import java.io.IOException
import java.nio.charset.StandardCharsets
import scala.scalanative.windows._
import scala.scalanative.windows.ErrorHandlingApi._
import scala.scalanative.windows.ErrorHandlingApiOps.errorMessage
import scala.scalanative.windows.WinBaseApi._

private[java] trait WindowsException extends Exception
private[java] object WindowsException {
  def apply(msg: String): WindowsException = {
    WindowsException(msg, GetLastError())
  }

  def apply(msg: String, errorCode: DWord): WindowsException = {
    new IOException(s"$msg - ${errorMessage(errorCode)} ($errorCode)")
      with WindowsException
  }

  def onPath(file: String): IOException = {
    import ErrorCodes._
    lazy val e = stdErrno.errno
    val winError = GetLastError()
    winError match {
      case _ if e == ENOTDIR   => new NotDirectoryException(file)
      case ERROR_ACCESS_DENIED => new AccessDeniedException(file)
      case ERROR_FILE_NOT_FOUND | ERROR_PATH_NOT_FOUND =>
        new NoSuchFileException(file)
      case ERROR_ALREADY_EXISTS => new FileAlreadyExistsException(file)
      case e =>
        new IOException(s"$file - ${errorMessage(winError)} ($winError)")
    }
  }

}
