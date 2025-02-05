package com.mobigen.vdap.springSample.sample;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith( MockitoExtension.class  )
class SampleControllerTest {
    SampleController controller;
    @Mock
    SampleService service;

    @Test
    void getSample() {
        controller = new SampleController( service );
        Mockito.when( service.getSample() ).thenReturn( "Hello World!" );
        assertEquals( controller.getSample(), "Hello World!",
                "SampleController.getSample() should return 'Hello World!'" );
    }
}