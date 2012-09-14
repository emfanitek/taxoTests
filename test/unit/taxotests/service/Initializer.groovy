package taxotests.service

import com.grailsrocks.taxonomy.TaxonomyService
import taxotests.testobjects.TestDomainClass1
import taxotests.TagReference

import static grails.test.MockUtils.mockLogging
import static java.util.Locale.FRANCE
import static java.util.Locale.UK
import taxotests.testobjects.TestDomainClass2

/**
 * lmuniz (9/13/12 12:39 AM)
 */
class Initializer {
    final static Locale SPAIN = new Locale('es', 'ES')

    def taxonomyService
    def taxonomyHelper

    def tagTranslationService
    def taggingService
    def translationService
    def searchService

    void initialize(List props, cut) {
        taxonomyService = new TaxonomyService()
        taxonomyHelper = new TaxonomyHelper(taxonomyService)

        translationService = new TranslationService()
        tagTranslationService = new TagTranslationService(
            availableLocales: [FRANCE, SPAIN, UK],
            translationService: translationService,
            taxonomyService: taxonomyService
        )
        taggingService = new TaggingService(
            tagTranslationService: tagTranslationService
        )

        taxonomyHelper.instrumentTaxonomyMethods([TestDomainClass1, TestDomainClass2, TagReference])

        taxonomyService.init()
        translationService.init()

        searchService = new SearchService(taxonomyService:taxonomyService)

        mockLogging(TaxonomyService)

        props.each {String prop ->
            cut[prop] = this."${prop}"
        }
    }
}
