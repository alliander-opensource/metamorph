; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.schemas.sql.datatype)

(def xsd->sql
  #:xsd{:string [:varchar 255]
        :boolean :boolean
        :decimal :decimal
        :float [:float 63 6]
        :int [:int 31]
        :double [:double 63 6]
        :duration [:double 63 6]
        :dateTime :datetime
        :date :datetime
        :time :datetime
        :anyURI [:varchar 255]})
