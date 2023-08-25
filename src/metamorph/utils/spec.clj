; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.utils.spec
  (:require [clojure.set :as set]))

(defn one-key-of [kws]
  (fn [m]
    (= (count (set/intersection (into #{} kws)
                                (into #{} (keys m))))
       1)))
