import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;

class GlvrdStatus implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

    @JsonProperty("code")
    public String code;

    @JsonProperty("name")
    public String name;

    @JsonProperty("message")
    public String message;

    @JsonProperty("max_text_length")
    public int max_text_length;

    @JsonProperty("max_hints_count")
    public int max_hints_count;

    @JsonProperty("period_underlimit")
    public boolean period_underlimit;

    @JsonProperty("frequency_underlimit")
    public boolean frequency_underlimit;

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}

class GlvrdHTTPResponse implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

    @JsonProperty("code")
    public String code;

    @JsonProperty("name")
    public String name;

    @JsonProperty("message")
    public String message;

    @JsonProperty("score")
    public String score;

    @JsonProperty("fragments")
    public List<Fragment> fragments;

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}

class Fragment {
    @JsonProperty("start")
    public int start;

    @JsonProperty("end")
    public int end;

    @JsonProperty("hint_id")
    public String hint_id;
}

class GlvrdHints implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

    @JsonProperty("message")
    public String message;

    @JsonProperty("hints")
    public JsonNode hints;

    @Override
    public String getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}

class GlvrdJSResponse implements GlvrdResponsable {
    @JsonProperty("status")
    public String status;

    @JsonProperty("code")
    public String code;

    @JsonProperty("name")
    public String name;

    @JsonProperty("message")
    public String message;

    @JsonProperty("score")
    public String score;

    @JsonProperty("fragments")
    public List<List<FragmentInJS>> fragments;

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    @JsonProperty("hints")
    public JsonNode hints;

    @JsonProperty("tabs")
    public List<Tab> tabs;

    @JsonProperty("stylesCss")
    public String stylesCss;
}

class Tab {
    @JsonProperty("code")
    public String code;

    @JsonProperty("name")
    public String name;

    @JsonProperty("textColor")
    public String textColor;

    @JsonProperty("lineColor")
    public String lineColor;
}

class FragmentInJS {
    @JsonProperty("start")
    public int start;

    @JsonProperty("end")
    public int end;

    @JsonProperty("hint")
    public String hint;
}

interface GlvrdResponsable {
    String getStatus();

    String getMessage();
}