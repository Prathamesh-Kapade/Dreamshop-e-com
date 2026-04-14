package com.demo.dreamshops.controller;

import com.demo.dreamshops.dto.ImageDto;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Image;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.service.image.IImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name="Image APIs")
@RequestMapping("${api.prefix}/images")
public class ImageController {
  private final IImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> saveImages(
            @Parameter(description = "Upload multiple images",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("files") List<MultipartFile> files,

            @RequestParam("productId") Long productId) {

        try {
            List<ImageDto> imageDtos = imageService.saveImages(files, productId);
            log.info("Returning DTO: {}", imageDtos);
            return ResponseEntity.ok(new ApiResponse("Upload successful", imageDtos));
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Upload failed!", null));
        }
    }

    @GetMapping("/image/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable Long imageId) {

        Image image = imageService.getImageById(imageId);

        ByteArrayResource resource = new ByteArrayResource(image.getImage());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + image.getFileName() + "\"")
                .body(resource);
    }

    @PostMapping(value = "/image/{imageId}/update", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> updateImage(
            @PathVariable Long imageId,
            @RequestParam("file") MultipartFile file) {

        try {
            imageService.updateImage(file, imageId);
            return ResponseEntity.ok(new ApiResponse("Update success!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Update failed!", INTERNAL_SERVER_ERROR));
        }
    }

    @DeleteMapping("/image/{imageId}/delete")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable Long imageId) {
        try {
            Image image = imageService.getImageById(imageId);
            if(image != null){
                imageService.deleteImageById(imageId);
            }
            return ResponseEntity.ok(new ApiResponse("delete success!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Delete failed!", INTERNAL_SERVER_ERROR));
        }
    }
}
