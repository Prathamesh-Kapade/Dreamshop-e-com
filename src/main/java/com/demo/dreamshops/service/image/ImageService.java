package com.demo.dreamshops.service.image;

import com.demo.dreamshops.dto.ImageDto;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Image;
import com.demo.dreamshops.model.Product;
import com.demo.dreamshops.repository.ImageRepository;
import com.demo.dreamshops.service.product.IProductService;
import com.demo.dreamshops.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService implements IImageService {

    private final ImageRepository imageRepository;
    private final IProductService productService;

    @Transactional(readOnly = true)
    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No image found with id: " + id));
    }

    @Override
    public void deleteImageById(Long id) {
        Image image = getImageById(id);
        imageRepository.delete(image);
    }

    @Override
    public List<ImageDto> saveImages(List<MultipartFile> files, Long productId) {

        Product product = productService.getProductById(productId);

        List<ImageDto> imageDtos = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Image image = new Image();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setImage(file.getBytes());
                image.setProduct(product);

                Image savedImage = imageRepository.save(image);

                imageDtos.add(convertToDto(savedImage));

            } catch (IOException e) {
                throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename());
            }
        }

        return imageDtos;
    }

    @Override
    public void updateImage(MultipartFile file, Long imageId) {

        Image image = getImageById(imageId);

        try {
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setImage(file.getBytes());

            imageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Failed to update image");
        }
    }

    private ImageDto convertToDto(Image image) {
        ImageDto dto = new ImageDto();
        dto.setId(image.getId());
        dto.setFileName(image.getFileName());
        dto.setFileType(image.getFileType());
        dto.setDownloadUrl("/api/v1/images/image/download/" + image.getId());
        return dto;
    }
}