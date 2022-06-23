(ns schema-transformer.schemas.sql.schema
  (:require [clojure.string :as string]
            [honey.sql :as sql]
            [schema-transformer.rdf.datatype :refer [rdf-list->seq]]
            [honey.sql.helpers :as h]
            [schema-transformer.graph.shacl :as shacl]
            [schema-transformer.schemas.sql.datatype :refer [xsd->sql]]))

(defn min-count [p]
  (min 1 (p :sh/minCount 0)))

(defn max-count [p]
  (if (= 1 (p :sh/maxCount)) 1 ##Inf))

(defn node-name [n]
  (shacl/class-name (n :sh/targetClass)))

(defn property-name [p]
  (shacl/class-name (p :sh/path)))

(defn primary-key? [p]
  (= (p :rdfs/comment) "PrimaryKey"))

(defn primary-key [n]
  (some->> (n :sh/property)
           (filter primary-key?)
           first))

(defn foreign-key? [p]
  (let [node-props (get-in p [:sh/node :sh/property])]
    (and (not-empty node-props)
         (some primary-key? node-props))))

(defn foreign-key [p]
  (primary-key (p :sh/node)))

(defn enum? [n]
  (contains? n :sh/in))

(defn node-shape->enum [n]
  (let [enum-members (fn [n] (map shacl/class-name
                          (rdf-list->seq (:sh/in n))))
        create-ddl (h/create-table (node-name n)
                                   [:value [:varchar 255] [:primary-key]])
        insert-ddl (-> (h/insert-into (node-name n))
                       (h/values [(into [] (enum-members n))]))]
    [create-ddl insert-ddl]))

(defn property-shape->column [p]
  (cond-> [(property-name p)]
    (contains? p :sh/datatype) (conj (xsd->sql (p :sh/datatype)))
    (primary-key? p) (conj [:primary-key])
    (contains? p :sh/node) (conj (if (enum? (p :sh/node))
                                   [:varchar 255]
                                   (xsd->sql ((foreign-key p) :sh/datatype)))
                                 [:foreign-key] [:references
                                                 (node-name (p :sh/node))
                                                 (if (foreign-key? p)
                                                   (property-name (foreign-key p))
                                                   :value)])
    (= 0 (min-count p)) (conj nil)))

(defn node-shape->table [n]
  (h/create-table (node-name n)
                  (h/with-columns (map property-shape->column
                                       (filter #(= 1 (max-count %))
                                               (shacl/properties n))))))

(defn node-shape->link-table [left-node p]
  (let [left-table (name (node-name left-node))
        left-datatype (xsd->sql ((primary-key left-node) :sh/datatype))
        left-primary-key (name (property-name (primary-key left-node)))
        left-col (keyword (str left-table "_" left-primary-key))
        right (cond
                (contains? p :sh/datatype) (name (property-name p))
                (contains? p :sh/node) (name (node-name (p :sh/node))))
        right-datatype (cond (contains? p :sh/datatype) (xsd->sql (p :sh/datatype))
                             (contains? p :sh/node) (xsd->sql ((foreign-key p) :sh/datatype)))
        right-primary-key (when (contains? p :sh/node) (name (property-name (foreign-key p))))
        right-col (cond
                    (contains? p :sh/datatype) :value
                    (contains? p :sh/node) (keyword (str right "_" right-primary-key)))]

    (-> (h/create-table (property-name p))
        (h/with-columns
          [[left-col left-datatype [:foreign-key] [:references (keyword left-table) (keyword left-primary-key)]]
           [right-col right-datatype (when (some? right-primary-key)
                                       [:foreign-key]
                                       [:references (keyword right) (keyword right-primary-key)])]
           [[:constraint (keyword (str (name (property-name p)) "_" "pkey"))] [:primary-key left-col right-col]]]))))

(defn node-shape->link-tables [n]
  (reduce conj [] (map #(node-shape->link-table n %)
                       (filter #(> (max-count %) 1)
                               (shacl/properties n)))))

(defn ->ddl [ns]
  (reduce conj [] (map #(if (enum? %) (node-shape->enum %)
                            ((juxt node-shape->table
                                   node-shape->link-tables) %))
                       ns)))




(defn ->schema [node-shapes]
  (str
   (->> (->ddl node-shapes)
        flatten
        (map #(first (sql/format % {:pretty true})))
        (string/join ";"))
   ";"))