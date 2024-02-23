package com.example.musicapp

    class Utilities {

        fun milliSecondsToTimer(milliseconds: Long): String {
            var finalTimerString = ""
            var secondsString = ""

            val hours = (milliseconds / (1000 * 60 * 60)).toInt()
            val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
            val seconds = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000).toInt()

            // Thêm giờ nếu có
            if (hours > 0) {
                finalTimerString = "$hours:"
            }

            // Thêm 0 nếu như giây có thêm một chữ số
            secondsString = if (seconds < 10) {
                "0$seconds"
            } else {
                "$seconds"
            }

            finalTimerString = "$finalTimerString$minutes:$secondsString"

            return finalTimerString
        }

        fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Int {
            var percentage = 0.0

            val currentSeconds = (currentDuration / 1000).toDouble()
            val totalSeconds = (totalDuration / 1000).toDouble()

            // tính phần trăm
            percentage = (currentSeconds / totalSeconds) * 100

            return percentage.toInt()
        }


        fun progressToTimer(progress: Int, totalDuration: Int): Int {
            var currentDuration = 0

            val totalSeconds = totalDuration / 1000
            currentDuration = ((progress.toDouble() / 100) * totalSeconds).toInt()

            return currentDuration * 1000
        }

}