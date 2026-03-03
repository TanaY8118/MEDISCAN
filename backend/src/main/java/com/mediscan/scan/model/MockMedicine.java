package com.mediscan.scan.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockMedicine {

    private String name;
    private String dosage;
    private String form;

    public static final MockMedicine UNKNOWN = new MockMedicine(
            "Unknown Medicine",
            "N/A",
            "UNKNOWN");
}
