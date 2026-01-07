package com.example.collegeattendanceapp.Conts


import android.location.Location
import com.google.android.gms.maps.model.LatLng

object GeofenceUtils {

    fun isInsideRectangularArea(
        userLat: Double,
        userLon: Double,
        areaBounds: RectangularArea
    ): Boolean {
        return userLat >= areaBounds.southWest.latitude &&
                userLat <= areaBounds.northEast.latitude &&
                userLon >= areaBounds.southWest.longitude &&
                userLon <= areaBounds.northEast.longitude
    }

    fun isInsidePolygon(
        userLat: Double,
        userLon: Double,
        polygonPoints: List<LatLng>
    ): Boolean {
        var crossings = 0
        val n = polygonPoints.size

        for (i in 0 until n) {
            val current = polygonPoints[i]
            val next = polygonPoints[(i + 1) % n]

            if (((current.latitude > userLat) != (next.latitude > userLat)) &&
                (userLon < (next.longitude - current.longitude) *
                        (userLat - current.latitude) / (next.latitude - current.latitude) + current.longitude)) {
                crossings++
            }
        }

        return crossings % 2 == 1
    }

    fun distanceToAreaEdge(
        userLat: Double,
        userLon: Double,
        areaBounds: RectangularArea
    ): Double {
        val distances = listOf(
            distanceToLatitudeLine(userLat, userLon, areaBounds.northEast.latitude),
            distanceToLatitudeLine(userLat, userLon, areaBounds.southWest.latitude),
            distanceToLongitudeLine(userLat, userLon, areaBounds.northEast.longitude),
            distanceToLongitudeLine(userLat, userLon, areaBounds.southWest.longitude)
        )

        return distances.minOrNull() ?: Double.MAX_VALUE
    }

    private fun distanceToLatitudeLine(userLat: Double, userLon: Double, targetLat: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLon, targetLat, userLon, results)
        return results[0].toDouble()
    }

    private fun distanceToLongitudeLine(userLat: Double, userLon: Double, targetLon: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLon, userLat, targetLon, results)
        return results[0].toDouble()
    }

    data class RectangularArea(
        val northEast: LatLng,
        val southWest: LatLng,
        val name: String = "Attendance Area"
    )

    data class PolygonArea(
        val points: List<LatLng>,
        val name: String = "Attendance Area"
    )
}
//Top 6 Skills to Add (Cloud Engineer Focused — Resume Ready)
//1. Linux Server Administration
//
//Managing Linux servers (Ubuntu), installing & configuring software, working with VMs, and handling Nginx/Apache.
//
//2. Cloud Platform Deployment
//
//Hands-on experience with Azure Virtual Machines, server setup, hosting applications, and managing cloud resources.
//
//3. Web Server & Application Hosting
//
//Configuring Nginx and Apache, deploying static and dynamic applications, setting up LAMP stack, and running Node/Python/Java apps on Linux.
//
//4. DNS & Domain Management
//
//Subdomains, MX records, email forwarding, A/AAAA/CNAME records, and full DNS setup for websites and services.
//
//5. SSL Certificate Configuration
//
//Installing and configuring SSL certificates (Let’s Encrypt & manual), enabling HTTPS for Nginx/Apache.
//
//6. cPanel Hosting & WordPress Management
//
//Deploying websites using cPanel: managing files, bandwidth, email settings, subdomains, and installing WordPress.
//
//Version Control & GitHub Deployment
//
//Cloning and deploying React, Angular, and Node.js projects from GitHub on cloud servers.