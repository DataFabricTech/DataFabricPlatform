import org.jsonschema2pojo.SourceType
import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.InclusionLevel

plugins {
    id("com.mobigen.java-library")
    id("org.jsonschema2pojo")
}

group = "${group}.share"
version = "1.0.0"

// Custom Annotation : Reflection 처리와 Password 필드로 인해 필요
buildscript {
    dependencies {
        // classpath(project(":annotator")) -> 실패
        // classpath("com.mobigen.vdap.share:annotator")) -> 실패
        // classpath(files("com.mobigen.vdap.share:annotator")) -> 실패
        // classpath(files("${rootProject.projectDir}/annotator/build/classes")) -> 싶패
        // 아래와 같이 com 으로 시작하는 폴더까지 지정해줘야 classloader 에 의해 로드되고
        // jsonschema2pojo 의 customAnnotator 에 설정할 수 있게 됨.
        classpath(files("${rootProject.projectDir}/annotator/build/classes/java/main"))
    }
}

dependencies {
    annotationProcessor(platform("com.mobigen.platform:product-platform"))
    implementation(platform("com.mobigen.platform:product-platform"))
    // Lombok
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")

    // Jsonschema
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    implementation("jakarta.validation:jakarta.validation-api")

    // Custom Annotation : Reflection 처리와 Password 필드로 인해 필요
    implementation(project(":annotator"))
    implementation("org.jsonschema2pojo:jsonschema2pojo-core")
    implementation("org.glassfish.jaxb:codemodel")
}

jsonSchema2Pojo {

    // Iterable<File> sourceFiles
    // Location of the JSON Schema file(s). This may refer to a single file or a directory of files.
    sourceFiles = files("${project.projectDir}/src/main/resources/json/schema")

    //  File targetDirectory
    // Target directory for generated Java source files. The plugin will add this directory to the
    // java source set so the compiler will find and compile the newly generated source files.
    // targetDirectory = layout.buildDirectory.dir("generated-sources/models").get().asFile

    // String targetPackage
    // Package name used for generated Java classes (for types where a fully qualified name has not
    // been supplied in the schema using the 'javaType' property).
    targetPackage = "com.mobigen.vdap.schema"

    // AnnotationStyle annotationStyle
    // The style of annotations to use in the generated Java types. Supported values:
    //  - jackson (alias of jackson2)
    //  - jackson2 (apply annotations from the Jackson 2.x library)
    //  - jsonb (apply annotations from the JSON-B 1 library)
    //  - jsonb2 (apply annotations from the JSON-B 2 library)
    //  - gson (apply annotations from the Gson library)
    //  - moshi1 (apply annotations from the Moshi 1.x library)
    //  - none (apply no annotations at all)
    setAnnotationStyle(AnnotationStyle.JACKSON.toString())

    // boolean useTitleAsClassname
    // Whether to use the 'title' property of the schema to decide the class name (if not
    // set to true, the filename and property names are used).
    useTitleAsClassname = false


    //  InclusionLevel inclusionLevel
    // The Level of inclusion to set in the generated Java types (for Jackson serializers)
    setInclusionLevel(InclusionLevel.NON_NULL.toString())

    // String classNamePrefix
    // Whether to add a prefix to generated classes.
    classNamePrefix = ""

    // String classNameSuffix
    // Whether to add a suffix to generated classes.
    classNameSuffix = ""

    // String[] fileExtensions
    // An array of strings that should be considered as file extensions and therefore not included in class names.


    // Class<? extends RuleFactory> customRuleFactory
    // A class that extends org.jsonschema2pojo.rules.RuleFactory and will be used to
    // create instances of Rules used for code generation.

    // boolean generateBuilders
    // Whether to generate builder-style methods of the form withXxx(value) (that return this),
    // alongside the standard, void-return setters.
    generateBuilders = true

    // boolean includeJsonTypeInfoAnnotation
    // Whether to include json type information; often required to support polymorphic type handling.
    // By default the type information is stored in the @class property, this can be overridden using
    // deserializationClassProperty in the schema
    // ex> includeJsonTypeInfoAnnotation = false

    // boolean useInnerClassBuilders
    // If set to true, then the gang of four builder pattern will be used to generate builders on
    // generated classes. Note: This property works in collaboration with generateBuilders.
    // If generateBuilders is false then this property will not do anything.
    // ex> useInnerClassBuilders = false

    // boolean includeConstructorPropertiesAnnotation
    // Whether to include java.beans.ConstructorProperties on generated constructors
    // ex> includeConstructorPropertiesAnnotation = false

    // boolean includeGetters
    // Whether to include getters or to omit these accessor methods and create public fields instead.
    includeGetters = true

    // boolean includeSetters
    // Whether to include setters or to omit these accessor methods and create public fields instead.
    includeSetters = true

    // boolean includeAdditionalProperties
    // Whether to allow 'additional' properties to be supported in classes by adding a map to
    // hold these. This is true by default, meaning that the schema rule 'additionalProperties'
    // controls whether the map is added. Set this to false to globally disable additional properties.
    // ex> includeAdditionalProperties = false

    // boolean includeDynamicAccessors
    // Whether to include dynamic getters, setters, and builders or to omit these methods.
    // ex> includeDynamicAccessors = false

    // boolean includeDynamicGetters
    // Whether to include dynamic getters or to omit these methods.
    // ex> includeDynamicGetters = false

    // boolean includeDynamicSetters
    // Whether to include dynamic setters or to omit these methods.
    // ex> includeDynamicSetters = false

    // boolean includeDynamicBuilders
    // Whether to include dynamic builders or to omit these methods.
    // ex> includeDynamicBuilders = false

    // boolean includeConstructors
    // Whether to generate constructors or not.
    // ex> includeConstructors = false

    // boolean constructorsRequiredPropertiesOnly
    // Whether to include only 'required' fields in generated constructors
    // ex> constructorsRequiredPropertiesOnly = false

    // boolean includeRequiredPropertiesConstructor;
    // Whether to *add* a constructor that includes only 'required' fields, alongside other constructors.
    // This property is irrelevant if constructorsRequiredPropertiesOnly = true
    // ex> includeRequiredPropertiesConstructor = false

    // boolean includeAllPropertiesConstructor;
    // Whether to *add* a constructor that includes all fields, alongside other constructors.
    // This property is irrelevant if constructorsRequiredPropertiesOnly = true
    // ex> includeAllPropertiesConstructor = false

    // boolean includeCopyConstructor;
    // Include a constructor with the class itself as a parameter, with the expectation that all properties
    // from the originating class will assigned to the new class.
    // This property is irrelevant if constructorsRequiredPropertiesOnly = true
    // ex> includeCopyConstructor = false

    // boolean includeHashcodeAndEquals
    // Whether to include hashCode and equals methods in generated Java types.
    includeHashcodeAndEquals = true

    // boolean includeJsr303Annotations
    // Whether to include JSR-303/349 annotations (for schema rules like minimum, maximum, etc) in
    // generated Java types. Schema rules and the annotation they produce:
    //  - maximum = @DecimalMax
    //  - minimum = @DecimalMin
    //  - minItems,maxItems = @Size
    //  - minLength,maxLength = @Size
    //  - pattern = @Pattern
    //  - required = @NotNull
    // Any Java fields which are an object or array of objects will be annotated with @Valid to
    // support validation of an entire document tree.
    // ex> includeJsr303Annotations = false
    includeJsr303Annotations = true

    // boolean includeJsr305Annotations
    // Whether to include JSR-305 annotations, for schema rules like Nullable, NonNull, etc
    // ex> includeJsr305Annotations = false

    // boolean useOptionalForGetters
    // Whether to use java.util.Optional for getters on properties that are not required
    // ex> useOptionalForGetters = false

    // boolean includeToString
    // Whether to include a toString method in generated Java types.
    includeToString = true

    // String[] toStringExcludes
    // properties to exclude from generated toString
    // ex> toStringExcludes = ["someProperty"]

    // boolean initializeCollections
    // Whether to initialize Set and List fields as empty collections, or leave them as null.
    // initializeCollections = true

    // String outputEncoding
    // The character encoding that should be used when writing the generated Java source files
    outputEncoding = "UTF-8"

    // boolean parcelable
    // Whether to make the generated types Parcelable for Android
    // ex> parcelable = false

    // boolean serializable
    // Whether to make the generated types Serializable
    // ex> serializable = false

    // char[] propertyWordDelimiters
    // The characters that should be considered as word delimiters when creating Java Bean property
    // names from JSON property names. If blank or not set, JSON properties will be considered to
    // contain a single word when creating Java Bean property names.
    // ex> propertyWordDelimiters = [] as char[]

    // boolean removeOldOutput
    // Whether to empty the target directory before generation occurs, to clear out all source files
    // that have been generated previously. <strong>Be warned</strong>, when activated this option
    // will cause jsonschema2pojo to <strong>indiscriminately delete the entire contents of the target
    // directory (all files and folders)</strong> before it begins generating sources.
    removeOldOutput = true

    // SourceType sourceType
    // The type of input documents that will be read. Supported values:
    //  - jsonschema (schema documents, containing formal rules that describe the structure of JSON data)
    //  - json (documents that represent an example of the kind of JSON data that the generated Java types
    //          will be mapped to)
    //  - yamlschema (JSON schema documents, represented as YAML)
    //  - yaml (documents that represent an example of the kind of YAML (or JSON) data that the generated Java types
    //          will be mapped to)
    setSourceType(SourceType.JSONSCHEMA.toString())

    // String targetVersion
    // What Java version to target with generated source code (1.6, 1.8, 9, 11, etc).
    // By default, the version will be taken from the Gradle Java plugin's 'sourceCompatibility',
    // which (if unset) itself defaults to the current JVM version
    targetVersion = "21"

    // boolean useCommonsLang3
    // deprecated, since we no longer use commons-lang for equals, hashCode, toString
    // ex> useCommonsLang3 = false

    // boolean useDoubleNumbers
    // Whether to use the java type double (or Double) instead of float (or Float) when representing
    // the JSON Schema type 'number'.
    // ex> useDoubleNumbers = true

    // boolean useBigDecimals
    // Whether to use the java type BigDecimal when representing the JSON Schema type 'number'. Note
    // that this configuration overrides useDoubleNumbers
    // ex> useBigDecimals = false

    // boolean useJodaDates
    // Whether to use {@link org.joda.time.DateTime} instead of {@link java.util.Date} when adding
    // date type fields to generated Java types.
    // ex> useJodaDates = false

    // boolean useJodaLocalDates
    // Whether to use org.joda.time.LocalTime for format: date-time. For full control see dateType
    // ex> useJodaLocalDates = false

    // boolean useJodaLocalTimes
    // Whether to use org.joda.time.LocalDate for format: date
    // ex> useJodaLocalTimes = false

    // String dateTimeType
    // What type to use instead of string when adding string properties of format "date-time" to Java types
    dateTimeType = "java.time.LocalDateTime"

    // String dateType
    // What type to use instead of string when adding string properties of format "date" to Java types
    dateType = "java.time.LocalDate"

    // String timeType
    // What type to use instead of string when adding string properties of format "time" to Java types
    timeType = "java.time.LocalTime"

    // boolean useLongIntegers
    // Whether to use the java type long (or Long) instead of int (or Integer) when representing the
    // JSON Schema type 'integer'.
    // ex> useLongIntegers = false

    // boolean useBigIntegers
    // Whether to use the java type BigInteger when representing the JSON Schema type 'integer'. Note
    // that this configuration overrides useLongIntegers
    // ex> useBigIntegers = false

    // boolean usePrimitives
    // Whether to use primitives (long, double, boolean) instead of wrapper types where possible
    // when generating bean properties (has the side-effect of making those properties non-null).
    // ex> usePrimitives = false

    // Class<? extends Annotator> customAnnotator
    // A fully qualified class name, referring to a custom annotator class that implements
    // org.jsonschema2pojo.Annotator and will be used in addition to the one chosen
    // by annotationStyle. If you want to use the custom annotator alone, set annotationStyle to none.
    // configurations.get("customAnnotator").setCustomAnnotator("com.mobigen.vdap.annotator.JsonAnnotator")
//     setCustomAnnotator("com.mobigen.vdap.annotator.JsonAnnotator")
//    customAnnotator = Class.forName(clazz, true, this.class.classLoader)

    // FileFilter fileFilter
    // A customer file filter to allow input files to be filtered/ignored
    // fileFilter = new AllFileFilter()

    // Whether to add JsonFormat annotations when using Jackson 2 that cause format "date", "time", and "date-time"
    // fields to be formatted as yyyy-MM-dd, HH:mm:ss.SSS and yyyy-MM-dd'T'HH:mm:ss.SSSZ respectively. To customize these
    // patterns, use customDatePattern, customTimePattern, and customDateTimePattern config options or add these inside a
    // schema to affect an individual field
    isFormatDateTimes = true
    isFormatDates = true
    formatTimes = true

    // String customDatePattern
    // A custom pattern to use when formatting date fields during serialization. Requires support from
    // your JSON binding library.
    customDatePattern = "yyyy-MM-dd"

    // String customTimePattern
    // A custom pattern to use when formatting time fields during serialization. Requires support from
    // your JSON binding library.
    customTimePattern = "HH:mm:ss.SSS"

    // String customDateTimePattern
    // A custom pattern to use when formatting date-time fields during serialization. Requires support from
    // your JSON binding library.
    customDateTimePattern = "yyyy-MM-dd HH:mm:ss.SSS"

    // String refFragmentPathDelimiters
    // Which characters to use as 'path fragment delimiters' when trying to resolve a ref
    // ex> refFragmentPathDelimiters = "#/."

    // SourceSortOrder sourceSortOrder
    // A sort order to use when reading input files, one of SourceSortOrder.OS (allow the OS to decide sort
    // order), SourceSortOrder.FILES_FIRST or SourceSortOrder.SUBDIRS_FIRST
    // sourceSortOrder = SourceSortOrder.OS

    // Map<String, String> formatTypeMapping
    // A map offering full control over which Java type will be used for each JSON Schema 'format' value
    // ex> formatTypeMapping = [...]

    // boolean includeGeneratedAnnotation
    // Whether to include a javax.annotation.Generated (Java 8 and lower) or
    // javax.annotation.processing.Generated (Java 9+) in on generated types (default true).
    // See also: targetVersion.
    // includeGeneratedAnnotation = true

    // boolean useJakartaValidation
    // Whether to use annotations from jakarta.validation package instead of javax.validation package
    // when adding JSR-303 annotations to generated Java types
    useJakartaValidation = true
}

//tasks.named("generateJsonSchema2Pojo") {
//    dependsOn(":share:annotator")
//    val annotatorPath = file("${rootProject.projectDir}/annotator/build/classes/java/main")
//    if (annotatorPath.exists()) {
//        // Class<? extends Annotator> customAnnotator
//        // A fully qualified class name, referring to a custom annotator class that implements
//        // org.jsonschema2pojo.Annotator and will be used in addition to the one chosen
//        // by annotationStyle. If you want to use the custom annotator alone, set annotationStyle to none.
//        // configurations.get("customAnnotator").setCustomAnnotator("com.mobigen.vdap.annotator.JsonAnnotator")
//        jsonSchema2Pojo{setCustomAnnotator("com.mobigen.vdap.annotator.JsonAnnotator")}
//        println("Set Custom Annotator")
//    } else {
//        println("Annotator classes not found in the specified path: ${annotatorPath.path}")
//        throw Exception("Annotator classes not found. build first annotator")
//    }
//}

afterEvaluate {
    tasks.named("generateJsonSchema2Pojo") {
        doFirst {
            try {
                val annotator = "com.mobigen.vdap.annotator.JsonAnnotator"
                jsonSchema2Pojo.setCustomAnnotator(annotator)
            } catch (e: ClassNotFoundException) {
                println("⚠️ Warning: 클래스 를 찾을 수 없습니다. Annotator 를 먼저 빌드해주세요.")
                throw Exception("need build annotator")
            }
        }
    }
}