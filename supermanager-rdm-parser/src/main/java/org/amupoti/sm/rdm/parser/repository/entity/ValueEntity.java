package org.amupoti.sm.rdm.parser.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by Marcel on 10/08/2015.
 */

/**
 * Mean value for ranking as local, visitor together with received ranking as local and visitor
 */
@Entity
@Getter
@Setter
public class ValueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String type;
    private String val;
    private String valL;
    private String valV;
    private String valRec;
    private String valRecL;
    private String valRecV;

}
