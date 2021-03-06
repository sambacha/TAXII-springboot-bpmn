package io.digitalstate.taxii.model.collection.manifest;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import io.digitalstate.stix.helpers.StixDataFormats;
import io.digitalstate.taxii.common.json.views.AdminView;
import io.digitalstate.taxii.common.json.views.TaxiiSpecView;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Value.Immutable @Serial.Version(1L)
@Value.Style(typeImmutable = "TaxiiCollectionManifestEntry")
@JsonSerialize(as = TaxiiCollectionManifestEntry.class) @JsonDeserialize(builder = TaxiiCollectionManifestEntry.Builder.class)
@JsonPropertyOrder({"id", "date_added", "versions", "media_types"})
public interface TaxiiCollectionManifestEntryResource extends Serializable {

    @NotBlank
    @JsonProperty("id")
    @JsonView({TaxiiSpecView.class, AdminView.class})
    String getId();

    @JsonProperty("date_added") @JsonInclude(value = NON_EMPTY, content= NON_EMPTY)
    @JsonSerialize(using = InstantSerializer.class)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = StixDataFormats.TIMESTAMP_PATTERN, timezone = "UTC")
    @JsonView({TaxiiSpecView.class, AdminView.class})
    Optional<Instant> getDateAdded();

    @JsonProperty("versions") @JsonInclude(value = NON_EMPTY, content= NON_EMPTY)
    @JsonView({TaxiiSpecView.class, AdminView.class})
    Set<String> getVersions();

    @JsonProperty("media_types") @JsonInclude(value = NON_EMPTY, content= NON_EMPTY)
    @JsonView({TaxiiSpecView.class, AdminView.class})
    Set<String> getMediaTypes();


}
