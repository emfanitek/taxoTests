package taxotests.service

import com.grailsrocks.taxonomy.TaxonomyService
import taxotests.testobjects.TestDomainClass1
import taxotests.SemanticLink

import static grails.test.MockUtils.mockLogging
import static java.util.Locale.FRANCE
import static java.util.Locale.UK
import taxotests.testobjects.TestDomainClass2
import taxotests.domain.DomainClassInstrumentator

/**
 * lmuniz (9/13/12 12:39 AM)
 */
class Initializer {
    final static Locale SPAIN = new Locale('es', 'ES')

    def taxonomyService
    def taxonomyHelper

    def tagTranslationService
    def taggingService
    def mockTranslationService
    def searchService
    def taxonomyExtensionService
    def domainClassInstrumentator

    void initialize(List props, cut) {
        mockLogging(TaxonomyService)

        mockTranslationService = new MockTranslationService()
        mockTranslationService.init()

        taxonomyService = new TaxonomyService()
        taxonomyService.init()

        taxonomyExtensionService = new TaxonomyExtensionService(taxonomyService: taxonomyService)

        tagTranslationService = new TagTranslationService(
            availableLocales: [FRANCE, SPAIN, UK],
            translationService: mockTranslationService,
            taxonomyService: taxonomyService ,
            taxonomyExtensionService:taxonomyExtensionService
        )

        taggingService = new TaggingService(
            tagTranslationService: tagTranslationService
        )

        taxonomyHelper = new TaxonomyHelper(taxonomyService)
        taxonomyHelper.instrumentTaxonomyMethods([TestDomainClass1, TestDomainClass2, SemanticLink])


        searchService = new SearchService(
            tagTranslationService: tagTranslationService,
            taxonomyService:taxonomyService
        )

        domainClassInstrumentator=new DomainClassInstrumentator(
            taggingService: taggingService,
            searchService: searchService
        )

        props.each {String prop ->
            cut[prop] = this."${prop}"
        }
    }
}
