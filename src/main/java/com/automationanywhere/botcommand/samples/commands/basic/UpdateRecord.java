package com.automationanywhere.botcommand.samples.commands.basic;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.samples.Utils.HTTPRequest;
import com.automationanywhere.botcommand.samples.Utils.SNOWServer;
import com.automationanywhere.botcommand.samples.Utils.ServiceNowActions;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.EntryList.EntryListAddButtonLabel;
import com.automationanywhere.commandsdk.annotations.rules.EntryList.EntryListEmptyLabel;
import com.automationanywhere.commandsdk.annotations.rules.EntryList.EntryListEntryUnique;
import com.automationanywhere.commandsdk.annotations.rules.EntryList.EntryListLabel;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.DataType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.automationanywhere.commandsdk.model.AttributeType.ENTRYLIST;
import static com.automationanywhere.commandsdk.model.AttributeType.TEXT;
import static com.automationanywhere.commandsdk.model.DataType.STRING;

/**
 * @author James Dickson
 *
 */
@BotCommand
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "UpdateRecord",
        label = "Update a Record",
        node_label = "Update a Servicenow record in session {{sessionName}}",
        group_label = "Records",
        description = "Updates a record in the specified table",
        icon = "snow.svg",
        comment = true ,
        //background_color =  "#293E40",
        return_label = "Assign output to a string",
        return_type = STRING,
        return_description = "Returns sys_id in a string variable")

public class UpdateRecord {
    @Sessions
    private Map<String, Object> sessionMap;

    @Idx(index = "4.3", type = TEXT, name="NAME")
    @Pkg(label = "Name", default_value_type = DataType.STRING)
    @NotEmpty
    private String name;

    @Idx(index = "4.4", type = TEXT, name="VALUE")
    @Pkg(label = "Value", default_value_type = DataType.STRING)
    private String value;

    @Execute
    public StringValue action(
            @Idx(index = "1", type = TEXT) @Pkg(label = "Session name", default_value_type = STRING, default_value = "Default")
            @NotEmpty String sessionName,
            @Idx(index = "2", type = TEXT) @Pkg(label = "Table", default_value_type = STRING)
            @NotEmpty String table,
            @Idx(index = "3", type = TEXT) @Pkg(label = "Sys_Id", default_value_type = STRING)
            @NotEmpty String sys_id,
            @Idx(index = "4", type = ENTRYLIST, options = {
                    @Idx.Option(index = "4.1", pkg = @Pkg(title = "NAME", label = "ServiceNow key")),
                    @Idx.Option(index = "4.2", pkg = @Pkg(title = "VALUE", label = "Value")),
            })
            //Label you see at the top of the control
            @Pkg(label = "Values to update in record", description = "Use this action to add updates to a record, such as comments. " +
                    "e.g. key: comments, value: Please try to reboot your computer")
            //Header of the entry form
            @EntryListLabel(value = "Provide entry")
            //Button label which displays the entry form
            @EntryListAddButtonLabel(value = "Add entry")
            //Uniqueness rule for the column, this value is the TITLE of the column requiring uniqueness.
            @EntryListEntryUnique(value = "NAME")
            //Message to display in table when no entries are present.
            @EntryListEmptyLabel(value = "No values to return")
                    List<Value> values
    ) throws IOException, ParseException {
        SNOWServer snowServer = (SNOWServer) this.sessionMap.get(sessionName);
        String token = snowServer.getToken();
        String url = snowServer.getURL();
        String response = "";
        String errorMessage = "";
        JSONObject result;
        response = ServiceNowActions.updateRecord(url, token, table, sys_id, values);
        Object obj = new JSONParser().parse(response);
        JSONObject json_resp = (JSONObject) obj;
        if(json_resp.containsKey("error")){
            JSONObject errorObject =  (JSONObject) json_resp.get("error");
            errorMessage = errorObject.get("message").toString() + ", details: " + errorObject.get("detail").toString();
            throw new BotCommandException("ServiceNow did not find the record at the specific sys_id. " + errorMessage);
        }
        result = (JSONObject) json_resp.get("result");
        return new StringValue(result.get("sys_id").toString());
    }
    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
}
