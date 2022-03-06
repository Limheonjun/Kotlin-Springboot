package com.kotlinspring.controller

import com.kotlinspring.dto.CourseDTO
import com.kotlinspring.entity.Course
import com.kotlinspring.service.CourseService
import com.kotlinspring.util.courseDTO
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.web.reactive.server.WebTestClient
import java.lang.RuntimeException

@WebMvcTest(CourseController::class)
@AutoConfigureWebTestClient
class CourseControllerUnitTest {

    @TestConfiguration
    class ControllerTestConfig {
        @Bean
        fun courseServiceMock() = mockk<CourseService>()
    }

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var courseServiceMock : CourseService

    @Test
    @Disabled
    fun addCourse() {
        val courseDTO = CourseDTO(null, "Build Kotlin Api", "hj")

        every { courseServiceMock.addCourse((any<CourseDTO>()))} returns courseDTO(id = 1)

        val savedCourseDto = webTestClient
            .post()
            .uri("/v1/courses")
            .bodyValue(courseDTO)
            .exchange()
            .expectStatus().isCreated
            .expectBody(CourseDTO::class.java)
            .returnResult()
            .responseBody

        Assertions.assertTrue {
            savedCourseDto!!.id != null
        }
    }

    @Test
    fun addCourse_validation() {
        val courseDTO = CourseDTO(null, "", "")
        every { courseServiceMock.addCourse(any()) } returns courseDTO(id = 1)

        val response = webTestClient
            .post()
            .uri("/v1/courses")
            .bodyValue(courseDTO)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(String::class.java)
            .returnResult()
            .responseBody

        assertEquals("courseDTO.category must not be blank, courseDTO.name must not be blank", response)
    }

    @Test
    fun addCourse_runtimeException() {
        val courseDTO = CourseDTO(null, "Build Kotlin Api", "hj")
        val errorMessage = "Unexpected Error occurred"
        every { courseServiceMock.addCourse(any()) } throws RuntimeException(errorMessage)


        val response = webTestClient
            .post()
            .uri("/v1/courses")
            .bodyValue(courseDTO)
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody(String::class.java)
            .returnResult()
            .responseBody

        assertEquals(errorMessage, response)
    }

    @Test
    fun retrieveAllCourses() {

        every { courseServiceMock.retrieveAllCourses() }.returnsMany(
            listOf(courseDTO(id = 1),
                courseDTO(id = 2, name = "my kotlin course")
                )
        )

        val courseDTOs = webTestClient
            .get()
            .uri("/v1/courses")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(CourseDTO::class.java)
            .returnResult()
            .responseBody

        println("courseDTOs : $courseDTOs")
        Assertions.assertEquals(2, courseDTOs!!.size)

    }

    @Test
    fun updateCourse() {

        val course = Course(null, "Build Kotlin Api", "hj")
        every { courseServiceMock.updateCourse(any(), any()) } returns courseDTO(id = 100, name = "Course Update!")

        val updatedCourseDTO = CourseDTO(null, "Course Update!", "lhj")

        val updatedCourse = webTestClient
            .put()
            .uri("/v1/courses/{courseId}", course.id)
            .bodyValue(updatedCourseDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(CourseDTO::class.java)
            .returnResult()
            .responseBody

        Assertions.assertEquals("Course Update!", updatedCourse!!.name)
    }

    @Test
    @Disabled
    fun deleteCourse() {
        //아무런 리턴값이 없는 경우 사용
//        every { courseServiceMock.deleteCourse(any()) } just runs
//
//        val updatedCourse = webTestClient
//            .delete()
//            .uri("/v1/courses/{courseId}", 100)
//            .exchange()
//            .expectStatus().isNoContent
//
//        verify(exactly = 1) { courseServiceMock.deleteCourse(any()) }
    }

}