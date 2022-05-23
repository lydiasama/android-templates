import java.io.File

object NewProject {

    private const val ARGUMENT_DELIMITER = "="
    private const val DOT_SEPARATOR = "."
    private const val KEY_APP_NAME = "app-name"
    private const val KEY_PACKAGE_NAME = "package-name"
    private const val TEMPLATE_FOLDER_NAME = "CoroutineTemplate"
    private const val TEMPLATE_PACKAGE_NAME = "co.nimblehq.coroutine"
    private const val TEMPLATE_APP_NAME = "Coroutine Template"

    private const val APP_PATTERN = "^[A-Z\$][a-zA-Z0-9\$]*\$"
    private const val PACKAGE_PATTERN = "^[a-z]+(\\.[a-z][a-z0-9]*)*\$"

    private val modules = listOf("app", "data", "domain")

    private var appName = ""
    private var packageName = ""

    private val projectPath: String
        get() = rootPath + appName

    private val rootPath: String
        get() = System.getProperty("user.dir").replace("scripts", "")

    fun generate(args: Array<String>) {
        handleArguments(args)
    }

    private fun handleArguments(args: Array<String>) {
        val agrumentError = "ERROR: Invalid Agrument name: Ensure define argruments => app-name={\"MyProject\"} package-name={com.sample.myproject}"
        when (args.size) {
            1 -> when {
                args.first().startsWith(KEY_APP_NAME) -> showMessage("ERROR: No package has been provided")
                args.first().startsWith(KEY_PACKAGE_NAME) -> showMessage("ERROR: No app name has been provided")
                else -> showMessage(agrumentError)
            }
            2 -> args.map { arg ->
                val (key, value) = arg.split(ARGUMENT_DELIMITER)
                when (key) {
                    KEY_APP_NAME -> validAppName(value)
                    KEY_PACKAGE_NAME -> validPackageName(value)
                    else -> showMessage(agrumentError)
                }.also { executeNextSteps() }
            }
            else -> showMessage("ERROR: Require app-name and package-name to initialize the new project")
        }
    }

    private fun validAppName(value: String) {
        if (APP_PATTERN.toRegex().containsMatchIn(value)) {
            appName = value
        } else {
            showMessage("ERROR: Invalid App Name: $value (needs to follow standard pattern {AppName})")
        }
    }

    private fun validPackageName(value: String) {
        if (PACKAGE_PATTERN.toRegex().containsMatchIn(value)) {
            packageName = value
        } else {
            showMessage("ERROR: Invalid Package Name: $value (needs to follow standard pattern {com.example.package})")
        }
    }

    private fun executeNextSteps() {
        if (appName.isNotEmpty() && packageName.isNotEmpty()) {
            initializeNewProjectFolder()
            cleanNewProjectFolder()
            renamePackageNameFolders()
            renamePackageNameWithinFiles()
            renameAppName()
        }
    }

    private fun initializeNewProjectFolder() {
        showMessage("=> 🐢 Initializing new project...")
        copyFiles(fromPath = rootPath + TEMPLATE_FOLDER_NAME, toPath = projectPath)
        // Set gradlew file as executable, because copying files from one folder to another doesn't copy file permissions correctly (= read, write & execute).
        File(projectPath + File.separator + "gradlew")?.setExecutable(true)
    }

    private fun renamePackageNameFolders() {
        showMessage("=> 🔎 Rename the package folders...")
        modules.forEach { module ->
            val srcPath = projectPath + File.separator + module + File.separator + "src"
            File(srcPath)
                .walk()
                .maxDepth(2)
                .filter { it.isDirectory && it.name == "java" }
                .forEach { javaDirectory ->
                    val oldDirectory = File(
                        javaDirectory, TEMPLATE_PACKAGE_NAME.replace(
                            oldValue = DOT_SEPARATOR,
                            newValue = File.separator
                        )
                    )
                    val newDirectory = File(
                        javaDirectory, packageName.replace(
                            oldValue = DOT_SEPARATOR,
                            newValue = File.separator
                        )
                    )

                    val tempDirectory = File(javaDirectory, "temp_directory")
                    oldDirectory.copyRecursively(tempDirectory)
                    oldDirectory.parentFile?.parentFile?.deleteRecursively()
                    newDirectory.mkdirs()
                    tempDirectory.copyRecursively(newDirectory)
                    tempDirectory.deleteRecursively()
                }
        }
    }

    private fun copyFiles(fromPath: String, toPath: String) {
        val targetFolder = File(toPath)
        val sourceFolder = File(fromPath)
        sourceFolder.copyRecursively(targetFolder, true) { file, exception ->
            showMessage(exception?.message ?: "Error copying files")
            return@copyRecursively OnErrorAction.TERMINATE
        }
    }

    private fun cleanNewProjectFolder() {
        executeCommand("sh $projectPath${File.separator}gradlew -p $projectPath clean")
        executeCommand("sh $projectPath${File.separator}gradlew -p $projectPath${File.separator}buildSrc clean")
        listOf(".idea", ".gradle", "buildSrc${File.separator}.gradle", ".git").forEach {
            File("$projectPath${File.separator}$it")?.let { targetFile ->
                targetFile.deleteRecursively()
            }
        }
    }

    private fun executeCommand(command: String) {
        val process = Runtime.getRuntime().exec(command)
        process.inputStream.reader().forEachLine { println(it) }
    }

    private fun renamePackageNameWithinFiles() {
        showMessage("=> 🔎 Renaming package name within files...")
        File(projectPath)
            ?.walk()
            .filter { it.isFile && it.name != "debug.keystore" }
            .forEach { filePath ->
                rename(
                    sourcePath = filePath.toString(),
                    oldValue = TEMPLATE_PACKAGE_NAME,
                    newValue = packageName
                )
            }
    }

    private fun renameAppName() {
        showMessage("=> 🔎 Renaming app name...")
        File(projectPath)
            ?.walk()
            .filter { it.isFile && it.name == "strings.xml" }
            .forEach { filePath ->
                rename(
                    sourcePath = filePath.toString(),
                    oldValue = TEMPLATE_APP_NAME,
                    newValue = appName
                )
            }
    }

    private fun rename(sourcePath: String, oldValue: String, newValue: String) {
        val sourceFile = File(sourcePath)
        var sourceText = sourceFile.readText()
        sourceText = sourceText.replace(oldValue, newValue)
        sourceFile.writeText(sourceText)
    }

    private fun showMessage(message: String) {
        println("\n${message}\n")
    }
}

NewProject.generate(args)
