package io.jcloud.api;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.client.CustomResource;
import io.jcloud.api.model.CustomResourceDefinition;
import io.jcloud.api.model.CustomResourceSpec;
import io.jcloud.api.model.CustomResourceStatus;
import io.jcloud.core.BaseService;

public class OperatorService<T extends Service> extends BaseService<T> {

    private List<CustomResourceDefinition> crds = new ArrayList<>();

    public List<CustomResourceDefinition> getCrds() {
        return crds;
    }

    public OperatorService<T> withCrd(String name, String crdFile,
            Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> type) {
        crds.add(new CustomResourceDefinition(name, crdFile, type));
        return this;
    }
}
