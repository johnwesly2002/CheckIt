package com.ecommerce.checkIt.service;


import com.ecommerce.checkIt.exceptions.ResourceNotFoundException;
import com.ecommerce.checkIt.model.Category;
import com.ecommerce.checkIt.model.Product;
import com.ecommerce.checkIt.payload.ProductDTO;
import com.ecommerce.checkIt.payload.ProductResponse;
import com.ecommerce.checkIt.repositories.CategoryRepository;
import com.ecommerce.checkIt.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public ProductDTO addProduct(Long CategoryId, ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);
        Category category = categoryRepository.findById(CategoryId).orElseThrow(() -> new ResourceNotFoundException("Category","categoryId", CategoryId));
        product.setCategory(category);
        double specialPrice = product.getPrice() - (product.getDiscount() * 0.01) * product.getPrice();
        product.setImage("default.png");
        product.setSpecialPrice(specialPrice);
        Product savedProduct =  productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productResponse = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        ProductResponse productResponseResponse = new ProductResponse();
        productResponseResponse.setContent(productResponse);
        return productResponseResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long CategoryId) {
        Category category = categoryRepository.findById(CategoryId).orElseThrow(() -> new ResourceNotFoundException("Category","categoryId", CategoryId));
        List<Product> products = productRepository.findByCategoryOrderByPriceAsc(category);
        List<ProductDTO> productResponse = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        ProductResponse productResponseResponse = new ProductResponse();
        productResponseResponse.setContent(productResponse);
        return productResponseResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword) {
        List<Product> products = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%');
        List<ProductDTO> productResponse = new ArrayList<>();
        products.forEach(product -> productResponse.add(modelMapper.map(product, ProductDTO.class)));
        ProductResponse productResponseResponse = new ProductResponse();
        productResponseResponse.setContent(productResponse);
        return productResponseResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        //Get the existing product form DB
        //Update the product info with user shared
        //save to database
        Product product = modelMapper.map(productDTO, Product.class);
        Product existingProduct =  productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product","productId", productId));
        existingProduct.setProductName(product.getProductName());
        existingProduct.setProductDescription(product.getProductDescription());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());
        double specialPrice =  product.getPrice() - (product.getDiscount() * 0.01) * product.getPrice();
        existingProduct.setSpecialPrice(specialPrice);

        Product savedProduct =  productRepository.save(existingProduct);
        return modelMapper.map(existingProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product","productId", productId));
        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product","productId", productId));

        // Upload image to server
        // get the file name of uploaded image
        String path = "/images";
        String fileName = uploadImage(path, image);

        // updating the new file name to the product.
        product.setImage(fileName);

        //Save updated Product
        Product updatedProduct =  productRepository.save(product);

        // return productDTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    private String uploadImage(String path, MultipartFile image) throws IOException {
        //File names of current / original file

        String originalFilename = image.getOriginalFilename();

        //Generate a unique file name using random.uuid.
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFilename.substring(originalFilename.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;
        //check if path exist and create
        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdirs();
        }
        //Upload to server
        Files.copy(image.getInputStream(), Paths.get(filePath));
        //returning file name

        return fileName;
    }


}
