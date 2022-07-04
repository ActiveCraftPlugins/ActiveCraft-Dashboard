package de.cplaiz.activecraftdashboard.account

import de.cplaiz.activecraftdashboard.ActiveCraftDashboard
import de.cplaiz.activecraftdashboard.account.device.Platform
import de.cplaiz.activecraftdashboard.api.Routed
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object AccountRouting : Routed("/account") {
    override fun Route.handleReq() {
        post("/register-device") {
            val formParameters = call.receiveParameters()
            val accessCode = formParameters["access-code"]!!.uppercase()
            println("Registering device with access code $accessCode")
            val deviceMan = ActiveCraftDashboard.instance.deviceMan
            if (deviceMan.activeRegistrationCodes.contains(accessCode)) {
                val tokenDevicePair = deviceMan.registerDevice(
                    deviceMan.activeRegistrationCodes[accessCode]!!,
                    formParameters["device-name"].toString(),
                    Platform.values()[formParameters["platform"].toString().toInt()]
                )
                val token = tokenDevicePair.first
                val device = tokenDevicePair.second
                deviceMan.activeRegistrationCodes.remove(accessCode)
                call.response.cookies.append("activecraft_dashboard_device_id", device.id)
                call.response.cookies.append("activecraft_dashboard_token", token)
                call.respond(HttpStatusCode.OK, "Device registration successful")
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid access code")
            }
        }
    }
}