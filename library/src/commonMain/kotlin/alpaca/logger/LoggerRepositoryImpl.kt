package alpaca.logger

import co.touchlab.kermit.Logger


class LoggerRepositoryImpl() : LoggerRepository {
    private val tag = "kmpLog"
    override fun i(message: String) {
        Logger.i(tag) { message }
    }

    override fun w(message: String) {
        Logger.w(tag) { message }
    }

    override fun v(message: String) {
        Logger.v(tag) { message }
    }

    override fun d(message: String) {
        Logger.d(tag) { message }
    }

    override fun e(message: String) {
        Logger.e(tag) { message }
    }
}