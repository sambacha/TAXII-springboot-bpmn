package io.digitalstate.taxii.mongo.repository.impl.collectionobject;

import io.digitalstate.stix.bundle.BundleableObject;
import io.digitalstate.stix.common.StixInstant;
import io.digitalstate.stix.helpers.StixDataFormats;
import io.digitalstate.taxii.mongo.exceptions.CollectionObjectAlreadyExistsException;
import io.digitalstate.taxii.mongo.model.document.CollectionObjectDocument;
import io.digitalstate.taxii.mongo.repository.CollectionObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CollectionObjectRepositoryImpl implements CollectionObjectRepositoryCustom {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StixDataFormats.TIMESTAMP_PATTERN)
            .withZone(ZoneId.of(StixDataFormats.TIMEZONE));

    private final MongoTemplate template;

    @Autowired
    public CollectionObjectRepositoryImpl(MongoTemplate mongoTemplate) {
        this.template = mongoTemplate;
    }

    @Autowired
    private CollectionObjectRepository collectionObjectRepository;

    @Override
    public CollectionObjectDocument createCollectionObject(@NotNull CollectionObjectDocument collectionObjectDocument, @NotNull String targetTenantId) throws CollectionObjectAlreadyExistsException {
        if (!collectionObjectDocument.tenantId().equals(targetTenantId)){
            throw new IllegalArgumentException("CollectionObjectDocument's tenantId does not match the targetTenantId.  Document's tenantId and TargetTenantId constraint exists to ensure documents are not added into unexpected tenants");
        }

        Class<? extends BundleableObject> bundleableObjectClass = collectionObjectDocument.object().getClass();
        String MODIFIED_METHOD_NAME = "getModified";

        Optional<StixInstant> modified;
        try {
            Method hasModified = bundleableObjectClass.getMethod(MODIFIED_METHOD_NAME);
            modified = Optional.of((StixInstant) hasModified.invoke(collectionObjectDocument.object()));
        } catch (NoSuchMethodException ignore) {
            modified = Optional.empty();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to save mongo doc because cannot access hasModified field");
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Unable to save mongo doc  because cannot invoke hasModified field");
        }

        boolean objectAlreadyExists;
        if (modified.isPresent()){
            objectAlreadyExists = this.objectExists(collectionObjectDocument.object().getId(), modified.get().getInstant(), collectionObjectDocument.collectionId(), collectionObjectDocument.tenantId());
        } else {
            objectAlreadyExists = this.objectExists(collectionObjectDocument.object().getId(), null, collectionObjectDocument.collectionId(), collectionObjectDocument.tenantId());
        }

        if (objectAlreadyExists) {
            throw new CollectionObjectAlreadyExistsException(collectionObjectDocument.collectionId(), collectionObjectDocument.object().getId());
        } else {
            return template.insert(collectionObjectDocument);
        }
    }

    public Page<CollectionObjectDocument> findAllObjectsByCollectionId(@NotNull String collectionId, @Nullable String tenantId, @NotNull Pageable pageable) {
        Query query = new Query();

        query.addCriteria(Criteria.where("collection_id").is(collectionId));

        if (tenantId != null) {
            query.addCriteria(Criteria.where("tenant_id").is(tenantId));
        }

        query.with(pageable);

        List<CollectionObjectDocument> objects = template.find(query, CollectionObjectDocument.class);
        return PageableExecutionUtils.getPage(objects, pageable,
                () -> template.count(query, CollectionObjectDocument.class));
    }

    @Override
    public List<CollectionObjectDocument> findObjectByObjectId(@NotNull String objectId, @Nullable String collectionId, @Nullable String tenantId) {
        Query query = new Query();

        query.addCriteria(Criteria.where("object.id").is(objectId));

        Optional.ofNullable(collectionId).ifPresent(c ->
                query.addCriteria(Criteria.where("collection_id").is(collectionId)));

        Optional.ofNullable(tenantId).ifPresent(t ->
                query.addCriteria(Criteria.where("tenant_id").is(tenantId)));

        return template.find(query, CollectionObjectDocument.class);
    }

    @Override
    public boolean objectExists(@NotNull String objectId, @Nullable Instant modified, @Nullable String collectionId, @Nullable String tenantId) {
        Query query = new Query();

        query.addCriteria(Criteria.where("object.id").is(objectId));

        Optional.ofNullable(modified).ifPresent(m -> {
            query.addCriteria(Criteria.where("object.modified").is(formatter.format(modified)));
        });

        Optional.ofNullable(collectionId).ifPresent(c ->
                query.addCriteria(Criteria.where("collection_id").is(collectionId)));

        Optional.ofNullable(tenantId).ifPresent(t ->
                query.addCriteria(Criteria.where("tenant_id").is(tenantId)));

        return template.exists(query, CollectionObjectDocument.class);
    }

}
