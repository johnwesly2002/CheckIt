package com.ecommerce.checkIt.service;


import com.ecommerce.checkIt.exceptions.APIException;
import com.ecommerce.checkIt.exceptions.ResourceNotFoundException;
import com.ecommerce.checkIt.model.Cart;
import com.ecommerce.checkIt.model.Category;
import com.ecommerce.checkIt.model.Product;
import com.ecommerce.checkIt.payload.CartDTO;
import com.ecommerce.checkIt.payload.ProductDTO;
import com.ecommerce.checkIt.payload.ProductResponse;
import com.ecommerce.checkIt.repositories.CartRepository;
import com.ecommerce.checkIt.repositories.CategoryRepository;
import com.ecommerce.checkIt.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Value("${project.images}")
    private String path;

    @Value("${image.base.url}")
    private String imageBaseUrl;


    @Override
    public ProductDTO addProduct(Long CategoryId, ProductDTO productDTO) {

        boolean ifProductNotPresent = true;
        Category category = categoryRepository.findById(CategoryId).orElseThrow(() -> new ResourceNotFoundException("Category","categoryId", CategoryId));
        List<Product> products = category.getProducts();
        for(Product p : products){
            if(p.getProductName().equals(productDTO.getProductName())){
                ifProductNotPresent = false;
                break;
            }
        }
        if(ifProductNotPresent) {
            Product product = modelMapper.map(productDTO, Product.class);
            product.setCategory(category);
            double specialPrice = product.getPrice() - (product.getDiscount() * 0.01) * product.getPrice();
            product.setImage("default.png");
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }else {
            throw new APIException("Product already exists!!!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder, String keyword, String category) {
        Pageable pageDetails = getPageDetails(pageNumber, pageSize, sortBy, sortOrder);
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();
        if(keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + keyword.toLowerCase() + "%"));
        }
        if(category != null && !category.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("category").get("categoryName"), category));
        }
        Page<Product> productPage = productRepository.findAll(spec, pageDetails);
        List<Product> products = productPage.getContent();
        return getProductResponse(products, productPage,"No products found");
    }

    @Override
    public ProductResponse searchByCategory(Long CategoryId,Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {
        Category category = categoryRepository.findById(CategoryId).orElseThrow(() -> new ResourceNotFoundException("Category","categoryId", CategoryId));
        Pageable pageDetails = getPageDetails(pageNumber, pageSize, sortBy, sortOrder);
        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
        List<Product> products = productPage.getContent();
        return getProductResponse(products,productPage, "Products not found with categoryId " + CategoryId);
    }



    @Override
    public ProductResponse searchProductByKeyword(String keyword,Integer pageNumber, Integer pageSize,String sortBy,String sortOrder) {
        Pageable pageDetails = getPageDetails(pageNumber, pageSize, sortBy, sortOrder);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);
        List<Product> products = productPage.getContent();
        return getProductResponse(products, productPage,"Products not found with Keyword " + keyword);
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
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> products = cart.getCartItems().stream().map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();
            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();
        cartDTOS.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(existingProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product","productId", productId));
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));
        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product","productId", productId));

        // Upload image to server
        // get the file name of uploaded image
        String fileName = fileService.uploadImage(path, image);

        // updating the new file name to the product.
        product.setImage(fileName);

        //Save updated Product
        Product updatedProduct =  productRepository.save(product);

        // return productDTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    private ProductResponse getProductResponse(List<Product> products, Page<Product> productPage, String ExceptionMessage) {
        if(products.isEmpty()){
            throw new APIException(ExceptionMessage);
        }
        List<ProductDTO> productDTOList = products.stream().map(product -> {
            ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
            productDTO.setImage(constructImageUrl(product.getImage()));
            return productDTO;
        }).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    private Pageable getPageDetails(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    }

    private String constructImageUrl(String imageName) {
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + imageName : imageBaseUrl + "/" + imageName;
    }

}
