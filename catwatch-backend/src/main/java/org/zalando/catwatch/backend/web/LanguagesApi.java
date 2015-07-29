package org.zalando.catwatch.backend.web;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zalando.catwatch.backend.model.Language;
import org.zalando.catwatch.backend.repo.ProjectRepository;
import org.zalando.catwatch.backend.util.Constants;
import org.zalando.catwatch.backend.util.DataAggregator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RequestMapping(value = Constants.API_RESOURCE_LANGUAGES, produces = {APPLICATION_JSON_VALUE})
@Api(value = Constants.API_RESOURCE_LANGUAGES, description = "the languages API")
public class LanguagesApi {

	
	private static final Integer DEFAULT_LIMIT = 5;

    private static final Integer DEFAULT_OFFSET = 0;
    
    @Autowired
    ProjectRepository repository;

    @ApiOperation(
        value = "Project programming language",
        notes =
            "The languages endpoint returns information about the languages used for projects by selected Github Organizations order by the number of projects using the programming language.",
        response = Language.class, responseContainer = "List"
    )
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "An array of programming language used and count of projects using it."),
            @ApiResponse(code = 0, message = "Unexpected error")
        }
    )
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<Collection<Language>> languagesGet(
            @ApiParam(value = "List of github.com organizations to scan(comma seperated)", required = true)
            @RequestParam(value = Constants.API_REQUEST_PARAM_ORGANIZATIONS, required = false)
            final String organizations,
            @ApiParam(value = "Number of items to retrieve. Default is 5.")
            @RequestParam(value = Constants.API_REQUEST_PARAM_LIMIT, required = false)
            final Integer limit,
            @ApiParam(value = "Offset the list of returned results by this amount. Default is zero.")
            @RequestParam(value = Constants.API_REQUEST_PARAM_OFFSET, required = false)
            final Integer offset,
            @ApiParam(value = "query paramater for search query (this can be language name prefix)")
            @RequestParam(value = Constants.API_REQUEST_PARAM_Q, required = false)
            final String q) throws NotFoundException {

        List<Language> languages = DataAggregator.getMainLanguages(organizations, new LanguagePercentComparator(), repository, Optional.ofNullable(q));
        
        Integer limitVal = Optional.ofNullable(limit).orElse(DEFAULT_LIMIT);
        Integer offsetVal = Optional.ofNullable(offset).orElse(DEFAULT_OFFSET);
        
        List<Language> filteredLanguages = DataAggregator.filterLanguages(languages, limitVal, offsetVal);
        
//        //apply limit and offset parameter, if any
//        if( offset!=null || limit != null) {
//        	
//        	List<Language> languageSubset = new ArrayList<>();
//        	
//        	int start = offset == null ? 0 : offset;
//        	
//        	if(start<languages.size()){
//        		int end = limit == null ? languages.size()-1 : start+limit;
//            	
//            	if(end>=languages.size()) end = languages.size()-1;
//            	
//            	languageSubset = languages.subList(start, end);
//        	}
//        	
//            return new ResponseEntity<Collection<Language>>(languageSubset, HttpStatus.OK); 
//        }
        
        
        return new ResponseEntity<Collection<Language>>(filteredLanguages, HttpStatus.OK);
    }

    private class LanguagePercentComparator implements Comparator<Language> {

        @Override
        public int compare(final Language l1, final Language l2) {

        	if(l1.getProjectsCount()<l2.getProjectsCount()) return 1;
        	
        	return -1;
        	
        }

    }

}