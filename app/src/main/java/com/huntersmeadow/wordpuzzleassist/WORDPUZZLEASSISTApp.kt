package com.huntersmeadow.wordpuzzleassist

import com.huntersmeadow.wordpuzzleassist.workclasses.WordDictionary

/** The global app context.
 */
class WORDPUZZLEASSISTApp : android.app.Application() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        WordDictionary.instance()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-

    companion object {

        /** Static reference to the app-context. */
        private var instance: WORDPUZZLEASSISTApp? = null

        /**
         * Get static reference to the app-context.
         *
         * @return Reference to the global app-context.
         */
        fun appContext(): WORDPUZZLEASSISTApp {
            return instance!!
        }
    }
}
