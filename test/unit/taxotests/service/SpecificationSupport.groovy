package taxotests.service

import spock.lang.Specification
import taxotests.testobjects.TestDomainClass1
import grails.test.mixin.Mock
import taxotests.TagReference
import com.grailsrocks.taxonomy.Taxon
import com.grailsrocks.taxonomy.Taxonomy
import com.grailsrocks.taxonomy.TaxonLink
import taxotests.testobjects.TestDomainClass2
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

/**
 * lmuniz (9/14/12 11:50 AM)
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([TestDomainClass1, TestDomainClass2, TagReference, Taxon, Taxonomy, TaxonLink])
class SpecificationSupport extends Specification {
    protected o1
    protected o2
    protected static final T1 = 'T1'
    protected static final T2 = 'T2'
    protected static final T3 = 'T3'
    protected static Locale SPAIN = new Locale('es', 'ES')

    def setup() {
        o1 = new TestDomainClass1(name: 'o1').save()
        o2 = new TestDomainClass1(name: 'o2').save()
    }

    protected Set allTags(obj) {
        obj.taxonomies.collect {it.name} as Set
    }

    protected setsAreEqual(Collection expected, Collection actual) {
        actual as Set == expected as Set
    }

    protected hasExactlyOneTag(obj, String locale, String tag) {
        obj.hasTaxonomy(['by_locale', locale, tag])
        obj.taxonomies.size() == 1
    }
}
