package stuff;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
@Model
@Named("something")
public class SomeBean {
    private static final Logger logger = LogManager.getLogger(SomeBean.class);

    private BeanManager bm;

    @Inject
    public SomeBean(BeanManager bm) {
        this.bm = bm;
    }

    @Deprecated // proxy constructor
    SomeBean() {}

    public String getFoo() {
        return "42";
    }
}
