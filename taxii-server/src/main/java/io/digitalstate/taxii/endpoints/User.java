package io.digitalstate.taxii.endpoints;

import io.digitalstate.taxii.common.Headers;
import io.digitalstate.taxii.endpoints.context.TenantWebContext;
import io.digitalstate.taxii.mongo.exceptions.TenantDoesNotExistException;
import io.digitalstate.taxii.mongo.exceptions.UserDoesNotExistException;
import io.digitalstate.taxii.mongo.JsonUtil;
import io.digitalstate.taxii.mongo.model.document.TenantDocument;
import io.digitalstate.taxii.mongo.model.document.UserDocument;
import io.digitalstate.taxii.mongo.repository.TenantRepository;
import io.digitalstate.taxii.mongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * API Roots are implemented as a concept of a tenant.  A tenant does not have to be "within the taxii server itself".
 * A TenantDocument is just a linking/mapping to a tenant in this taxii server or another.
 */
@Controller
@RequestMapping("/taxii/tenant/{tenantSlug}")
public class User {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantWebContext tenantWebContext;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_USERS_VIEWER')")
    public ResponseEntity<String> getUser(@RequestHeader HttpHeaders headers,
                                          @RequestParam(name = "page", defaultValue = "0") Integer page
    ) throws TenantDoesNotExistException {

        //@TODO process headers for validation

        tenantWebContext.setDatabaseToDefaultTenantForCurrentThread();
        Page<UserDocument> users = userRepository.findAllUsersByTenantId(tenantWebContext.getTenantId(), PageRequest.of(page, 100));
        tenantWebContext.setDatabaseToTenantIdForCurrentThread();

        return ResponseEntity.ok()
                .headers(Headers.getSuccessHeaders())
                .body(JsonUtil.ListToJson(users.getContent()));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ROLE_USERS_VIEWER') or principal.username == #userId")
    public ResponseEntity<String> getUser(@RequestHeader HttpHeaders headers,
                                          @PathVariable("userId") String userId) throws TenantDoesNotExistException, UserDoesNotExistException {

        //@TODO process headers for validation
        tenantWebContext.setDatabaseToDefaultTenantForCurrentThread();
        UserDocument user = userRepository.findUserByUsername(userId, tenantWebContext.getTenantId())
                .orElseThrow(() -> new UserDoesNotExistException(userId));
        tenantWebContext.setDatabaseToTenantIdForCurrentThread();

        return ResponseEntity.ok()
                .headers(Headers.getSuccessHeaders())
                .body(user.toJson());
    }

}
