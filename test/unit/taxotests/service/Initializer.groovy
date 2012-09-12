package taxotests.service

import com.grailsrocks.taxonomy.TaxonomyService
import taxotests.Book
import taxotests.TagReference
import static java.util.Locale.*
import grails.test.mixin.Mock
import com.grailsrocks.taxonomy.Taxon
import com.grailsrocks.taxonomy.Taxonomy
import com.grailsrocks.taxonomy.TaxonLink
import static grails.test.MockUtils.*

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

        taxonomyHelper.instrumentTaxonomyMethods([Book, TagReference])

        taxonomyService.init()
        translationService.init()

        mockLogging(TaxonomyService)

        props.each{String prop->
            cut[prop]=this."${prop}"
        }
    }
}
