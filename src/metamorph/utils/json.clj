(ns metamorph.utils.json
  (:import (com.fasterxml.jackson.core JsonGenerator)))

(defn encode-keyword
  "Encode a keyword to the json generator."
  [^clojure.lang.Keyword k ^JsonGenerator jg]
  (.writeString jg (if-let [ns (namespace k)]
                     (str ns "." (name k))
                     (name k))))