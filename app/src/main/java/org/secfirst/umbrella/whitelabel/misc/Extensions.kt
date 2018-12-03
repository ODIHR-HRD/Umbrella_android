import Extensions.Companion.PERMISSION_REQUEST_EXTERNAL_STORAGE
import android.Manifest
import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityCompat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jakewharton.processphoenix.ProcessPhoenix
import org.secfirst.umbrella.whitelabel.UmbrellaApplication
import org.secfirst.umbrella.whitelabel.feature.main.MainActivity
import java.io.File
import kotlin.reflect.KClass

class Extensions {
    companion object {
        const val PERMISSION_REQUEST_EXTERNAL_STORAGE = 1
    }
}

fun MainActivity.requestExternalStoragePermission() {

    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_EXTERNAL_STORAGE)
    } else {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_EXTERNAL_STORAGE)
    }
}

fun doRestartApplication(context: Context) {
    val tourIntent = Intent(context, MainActivity::class.java)
    tourIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    ProcessPhoenix.triggerRebirth(context, tourIntent)
}

fun getAssetFileBy(fileName: String) = UmbrellaApplication.instance.assets.open(fileName)


fun <T : Any> parseYmlFile(file: File, c: KClass<T>): T {
    val mapper = ObjectMapper(YAMLFactory())
    mapper.registerModule(KotlinModule())
    return file.bufferedReader().use { mapper.readValue(it.readText(), c.java) }
}