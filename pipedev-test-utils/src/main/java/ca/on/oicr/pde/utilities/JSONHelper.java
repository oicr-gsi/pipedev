package ca.on.oicr.pde.utilities;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import com.sdicons.json.model.JSONObject;
import com.sdicons.json.model.JSONValue;
import com.sdicons.json.parser.JSONParser;
import com.sdicons.json.validator.JSONValidator;
import com.sdicons.json.validator.ValidationException;
import com.sdicons.json.validator.Validator;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * JSON Helper Class Contains methods related to parsing/validating of JSON which one may find useful
 *
 * @author Raunaq Suri
 *
 */
public class JSONHelper {

    /**
     * Validates a json file against a schema to see if the file is valid
     *
     * @param schemaJSON the json file which contains the schema
     * @param dataJSON the json file which you wish to validate
     * @return Whether or not the JSON is valid or not
     * @throws java.io.IOException if there is a json parsing error
     */
    public boolean isJSONValid(File schemaJSON, File dataJSON) throws IOException {
        
        if(schemaJSON == null || dataJSON == null){
            throw new IOException("Null input");
        }

        try {
            //Loads the schema and the data
            JSONParser schemaParser = new JSONParser(FileUtils.openInputStream(schemaJSON));
            JSONParser dataParser = new JSONParser(FileUtils.openInputStream(dataJSON));

            //Setup
            JSONObject validatorObj = (JSONObject) schemaParser.nextValue();

            Validator validator = new JSONValidator(validatorObj);

            JSONValue data = dataParser.nextValue();

            //Validates the data
            validator.validate(data);

            /*This is how the following section works
             First the json is read in as a string directly and is also parsed in.
             Then, the string version is compared to the parsed version
             If they are the same, that means that there were no duplicate elements
             Else, there were some.
             */
            //Reads in the JSON into a string and removes all whitespace
            String jsonActualString = FileUtils.readFileToString(dataJSON).trim().replaceAll(" ", "").replaceAll("\n", "");
            JSONParser parser = new JSONParser(FileUtils.openInputStream(dataJSON));
            JSONValue value = parser.nextValue();

            //Reads in the parsed json to a string and removes all whitespace
            String parsedJSON = value.render(false).replaceAll(" ", "").replaceAll("\n", "");

            //If they don't match, that means that a duplicate key value pair is detected
            if (!jsonActualString.equalsIgnoreCase(parsedJSON)) {
                System.out.println("Error: Duplicate key:value pair detected");
                return false;
            }

        } catch (TokenStreamException ex) {
            Logger.getLogger(JSONHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Token stream exception");
            System.err.println(ex.getMessage());
            return false;

        } catch (RecognitionException ex) {
            Logger.getLogger(JSONHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Not recognized");
            System.err.println(ex.getMessage());
            return false;
        } catch (ValidationException ex) {
            Logger.getLogger(JSONHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Not properly validated");
            System.err.println(ex.getMessage());
            return false;
        }

        //Returns true if there are no errors
        return true;
    }
}
