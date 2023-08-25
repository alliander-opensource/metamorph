; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.schemas.avro.datatype
  (:require [deercreeklabs.lancaster :as l]))

(def xsd->avro
  #:xsd{:string l/string-schema
        :double l/double-schema
        :boolean l/boolean-schema
        :int l/int-schema
        :integer l/int-schema
        :decimal l/float-schema
        :float l/float-schema
        :duration l/fixed-schema
        :dateTime l/string-schema
        :date l/string-schema
        :time l/string-schema
        :anyURI l/string-schema})
