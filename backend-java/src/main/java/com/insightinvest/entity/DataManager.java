package com.insightinvest.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DATA_MANAGER")
public class DataManager extends User {
}
