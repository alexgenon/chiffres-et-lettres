package api

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.ContentType
import twirl.api.{ Xml, Txt, Html }
import akka.http.scaladsl.model.MediaType

object TwirlSupport extends TwirlSupport

trait TwirlSupport {

  /** Serialize Twirl `Html` to `text/html`. */
  implicit val twirlHtmlMarshaller = twirlMarshaller[Html](`text/html`)

  /** Serialize Twirl `Txt` to `text/plain`. */
  implicit val twirlTxtMarshaller = twirlMarshaller[Txt](`text/plain`)

  /** Serialize Twirl `Xml` to `text/xml`. */
  implicit val twirlXmlMarshaller = twirlMarshaller[Xml](`text/xml`)

  /** Serialize Twirl formats to `String`. */
  protected def twirlMarshaller[A <: AnyRef: Manifest](contentType: MediaType): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(contentType)(_.toString)

}

/*
import spray.http.{ HttpEntity, StatusCode }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import spray.httpx.marshalling.{ MetaMarshallers, CollectingMarshallingContext, ToResponseMarshaller, Marshaller }
import spray.json.DefaultJsonProtocol



/**
  * Case class that represents an Error inside the application
  * @param code Status code returned in the response
  * @param entity Response entity
  */
case class ErrorResponseException(code: StatusCode, entity: HttpEntity) extends Throwable

/**
  * Main trait for Marshalling support
  */
trait Marshalling extends DefaultJsonProtocol with SprayJsonSupport with MetaMarshallers {

  /**
    * Function for handling errors when API returns Left(ERROR) or Right(Response)
    * This implementation was inspired by Darek's spray seed: https://github.com/darek/spray-slick-seed
    *  who references http://www.cakesolutions.net/teamblogs/2012/12/10/errors-failures-im-a-teapot
    *  as a good explanation.
    */
  implicit def eitherCustomMarshaller[A, B](code: StatusCode)(implicit ma: Marshaller[A], mb: Marshaller[B]): ToResponseMarshaller[Either[A, B]] = Marshaller[Either[A, B]] { (value, context) =>
    value match {
      case Left(a) =>
        val mc = new CollectingMarshallingContext()
        ma(a, mc)
        context.handleError(ErrorResponseException(code, mc.entity))
      case Right(b) =>
        (200, mb(b, context))
    }
  }
}*/


