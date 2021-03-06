package br.com.casadocodigo.loja.controllers;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import br.com.casadocodigo.loja.daos.ProductDAO;
import br.com.casadocodigo.loja.infra.FileSaver;
import br.com.casadocodigo.loja.models.BookType;
import br.com.casadocodigo.loja.models.Product;

@Controller
@Transactional
@RequestMapping("/produtos")
public class ProductsController {

	@Autowired
	private ProductDAO products;
	@Autowired
	private FileSaver fileSaver;
	@Autowired
	private ServletContext ctx;
	@Autowired
	private InternalResourceViewResolver resolver;
	

	@RequestMapping(method=RequestMethod.POST)
	@CacheEvict(value="lastProducts", allEntries=true)
	public ModelAndView save(MultipartFile summary,@ModelAttribute("product") @Valid Product product,BindingResult bindingResult,RedirectAttributes redirectAttributes) throws IOException{
		if(bindingResult.hasErrors()){
			return form(product);
		}
		
		//Sera que passo o product como parametro?
		String webPath = fileSaver.write("uploaded-images",summary);
		product.setSummaryPath(webPath);
		products.save(product);
		
		redirectAttributes.addFlashAttribute("success", "Produto cadastrado com sucesso");
		return new ModelAndView("redirect:produtos");
	}
	
	@RequestMapping("/form")
	public ModelAndView form(@ModelAttribute Product product){
		ModelAndView modelAndView = new ModelAndView("products/form");
		modelAndView.addObject("bookTypes", BookType.values());
		return modelAndView;
	}
	
	@RequestMapping(method=RequestMethod.GET)
	@Cacheable(value="lastProducts")
	public ModelAndView list() throws ServletException, IOException{
		System.out.println("ola");
		ModelAndView modelAndView = new ModelAndView("products/list");
		modelAndView.addObject("products", products.findAll());
		return modelAndView;
	}
	
	
	@RequestMapping("/{id}")
	public ModelAndView show(@PathVariable("id") Integer id){
		ModelAndView modelAndView = new ModelAndView("products/show");
		Product product = products.findOne(id);
		modelAndView.addObject("product", product);
		return modelAndView;
	}
	
}
