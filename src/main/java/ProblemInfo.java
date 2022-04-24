import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class ProblemInfo {
    @JsonProperty("descriptionTemplate")
    public String descriptionTemplate;

    @JsonProperty("fragmentStart")
    public int fragmentStart;

    @JsonProperty("fragmentEnd")
    public int fragmentEnd;

    ProblemInfo() {
        // для сериализации нужен пустой конструктор
    }

    ProblemInfo(@NotNull final String descriptionTemplate, final int fragmentStart, final int fragmentEnd) {
        this.descriptionTemplate = descriptionTemplate;
        this.fragmentStart = fragmentStart;
        this.fragmentEnd = fragmentEnd;
    }
}

class ProblemInfoConverter extends Converter<Map<String, ArrayList<ProblemInfo>>> {
    @Override
    public @Nullable Map<String, ArrayList<ProblemInfo>> fromString(@NotNull String value) {
        var temp = new HashMap<String, ArrayList<ProblemInfo>>();
        if (value.equals("")) {
            return temp;
        }
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(value, new TypeReference<HashMap<String, ArrayList<ProblemInfo>>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return temp;
    }

    @Override
    public @Nullable String toString(@NotNull Map<String, ArrayList<ProblemInfo>> value) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}