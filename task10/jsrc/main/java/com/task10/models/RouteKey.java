package com.task10.models;

import java.util.Objects;

public class RouteKey {

    private String method;
    private String path;

    public RouteKey(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "RouteKey{" +
            "method='" + method + '\'' +
            ", path='" + path + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RouteKey routeKey = (RouteKey) o;
        return Objects.equals(path, routeKey.path) &&
            Objects.equals(method, routeKey.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }
}
