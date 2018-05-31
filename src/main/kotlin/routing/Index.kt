package routing

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.routing.Routing
import io.ktor.routing.get
import views.MulticolumnTemplate

fun Routing.index(){
    get("/"){
        call.respondHtmlTemplate(MulticolumnTemplate()) {
            column1 {
                +"Authors"
            }
            column2 {
                +"Stanisław Barański"
            }
        }
    }
}