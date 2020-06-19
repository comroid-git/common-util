package org.comroid.restless.server.auth;

import org.comroid.restless.REST;
import org.comroid.uniform.node.UniNode;

import java.util.function.Function;

public interface AuthenticationMethod<S> extends Function<S, String> {
    @Override
    String apply(S s);

    final class Header implements AuthenticationMethod<REST.Header> {
        @Override
        public String apply(REST.Header header) {
            return header.getValue();
        }
    }

    final class UrlParam implements AuthenticationMethod<String[]> {
        private final int index;

        public UrlParam(int index) {
            this.index = index;
        }

        @Override
        public String apply(String[] params) {
            return params[index];
        }
    }

    final class Body implements AuthenticationMethod<UniNode> {
        private final Function<UniNode, String> extractor;

        public Body(Function<UniNode, String> extractor) {
            this.extractor = extractor;
        }

        @Override
        public String apply(UniNode uniNode) {
            return extractor.apply(uniNode);
        }
    }
}
