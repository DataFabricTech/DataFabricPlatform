package com.mobigen.monitoring.builder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlToEnum {

    public static final String MSG_FILE_PATH = "./src/main/resources/i18n/message.yml";
    public static final String JAVA_FILE_PATH = "./src/main/java/com/mobigen/monitoring/exception/ResponseCode.java";

    public static void main( String[] args ) {
        Logger log = Logger.getLogger( "Error Msg Yaml To Enum Class" );

        // Delete Old File
        try {
            cleanUp( Path.of( JAVA_FILE_PATH ) );
        } catch( IOException e ) {
            throw new RuntimeException( e );
        }

        try(
                FileInputStream fileInputStream = new FileInputStream( MSG_FILE_PATH );
                InputStreamReader inputStreamReader = new InputStreamReader( fileInputStream, StandardCharsets.UTF_8 );
                BufferedReader reader = new BufferedReader( inputStreamReader );

                // New File
                FileOutputStream fileOutputStream = new FileOutputStream( JAVA_FILE_PATH );
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter( fileOutputStream, StandardCharsets.UTF_8 );
                PrintWriter printWriter = new PrintWriter( outputStreamWriter ); ) {


            // Header
            printWriter.print( "package com.mobigen.monitoring.exception;\n\n" );
            printWriter.print( "import lombok.Getter;\n" );
            printWriter.print( "@Getter\n" );
            printWriter.print( "public enum ResponseCode {\n" );
            printWriter.print( "    SUCCESS(\"0\"),\n" );
            printWriter.print( "    ERROR_BIND(\"Error Bind\"),\n" );
            printWriter.print( "    ERROR_METHOD_NOT_SUPPORTED(\"METHOD NOT SUPPORTED\"),\n" );
            printWriter.print( "    ERROR_UNKNOWN( \"UNKNOWN\"),\n" );

            // Body
            Pattern pattern = Pattern.compile( "^[a-zA-Z]{3}\\d{4}" );
            String line = reader.readLine();
            while( line != null ) {
                Matcher matcher = pattern.matcher( line );
                if( matcher.find() ) {
                    String code = line.substring( matcher.start(), matcher.end() ).toUpperCase();
                    String message = line.substring( matcher.end() + 1 );
                    printWriter.println( String.format( "    %s( \"%s\"),         // %s", code, code, message ) );
                }
                line = reader.readLine();
            }

            // Tail
            printWriter.print( "    ;\n\n" );
            printWriter.print( "    private final String name;\n\n" );
            printWriter.print( "    ResponseCode( String name ) {\n" );
            printWriter.print( "        this.name = name;\n" );
            printWriter.print( "    }\n" );
            printWriter.print( "}\n" );
            printWriter.flush();

        } catch( IOException e ) {
            log.log( Level.WARNING, "failed to create yaml file", e );
        }
    }

    public static void cleanUp( Path path ) throws IOException {
        Files.delete( path );
    }
}
