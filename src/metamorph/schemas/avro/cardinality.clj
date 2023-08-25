; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.schemas.avro.cardinality
  (:require [deercreeklabs.lancaster :as l]))

(def cardinality->schema-fn
  {[1  1] identity
   [0  1] l/maybe
   [1 :*] l/array-schema
   [0 :*] (comp l/maybe l/array-schema)})
