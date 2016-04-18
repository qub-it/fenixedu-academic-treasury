package org.fenixedu.academictreasury.ui;

import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringApplication(group = "treasuryBackOffice | treasuryManagers", path = "academicTreasury", title = "title.AcademicTreasury", hint = "Academic Treasury")
@RequestMapping(value="/academicTreasury")
public class AcademicTreasuryController {

    @RequestMapping
    public String home(Model model) {
        return "academicTreasury/home";
    }
    
}
