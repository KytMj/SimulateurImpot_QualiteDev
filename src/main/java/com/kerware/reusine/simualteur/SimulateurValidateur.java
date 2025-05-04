package com.kerware.reusine.simualteur;

import com.kerware.simulateur.SituationFamiliale;

public class SimulateurValidateur {

    private static final int NOMBRE_MAX_ENFANTS = 7;

    public static void validiteParametresCalCulImpot(
            int revenuNetDeclarant1,
            int revenuNetDeclarant2,
            SituationFamiliale situationFamiliale,
            int nombreEnfants,
            int nombreEnfantsHandicapes,
            boolean parentIsole
    ) throws IllegalArgumentException {

        verificationValeurNonNegative(revenuNetDeclarant1,
                "Le revenu net ne peut pas être négatif");

        verificationValeurNonNegative(revenuNetDeclarant2,
                "Le revenu net ne peut pas être négatif");

        verificationValeurNonNegative(nombreEnfants,
                "Le nombre d'enfants ne peut pas être négatif");

        verificationValeurNonNegative(nombreEnfantsHandicapes,
                "Le nombre d'enfants handicapés ne peut pas être négatif");

        verificationNombreEnfants(nombreEnfants, nombreEnfantsHandicapes);

        boolean personneSeule = estSeule(situationFamiliale);

        verificationSituationFamiliale(
                situationFamiliale,
                personneSeule,
                parentIsole,
                revenuNetDeclarant2
        );
    }

    private static boolean estSeule(SituationFamiliale situationFamiliale){
        return (situationFamiliale == SituationFamiliale.CELIBATAIRE
                || situationFamiliale == SituationFamiliale.DIVORCE
                || situationFamiliale == SituationFamiliale.VEUF);
    }

    private static void verificationNombreEnfants(
            int nombreEnfants,
            int nombreEnfantsHandicapes
    ){
        if (nombreEnfantsHandicapes > nombreEnfants) {
            throw new IllegalArgumentException(
                    "Le nombre d'enfants handicapés ne peut pas être " +
                            "supérieur au nombre d'enfants"
            );
        }

        if (nombreEnfants > NOMBRE_MAX_ENFANTS) {
            throw new IllegalArgumentException(
                    "Le nombre d'enfants ne peut pas être supérieur à 7"
            );
        }
    }

    private static void verificationValeurNonNegative(
            int valeur,
            String message
    ) throws IllegalArgumentException{
        if(valeur < 0){
            throw new IllegalArgumentException(message);
        }
    }

    private static void verificationSituationFamiliale(
            SituationFamiliale situationFamiliale,
            boolean personneSeule,
            boolean parentIsole,
            int revenuNetDeclarant2
    ) throws IllegalArgumentException {
        if (situationFamiliale == null) {
            throw new IllegalArgumentException(
                    "La situation familiale ne peut pas être null"
            );
        }

        if (parentIsole
                && (situationFamiliale == SituationFamiliale.MARIE
                    || situationFamiliale == SituationFamiliale.PACSE)) {
            throw new IllegalArgumentException(
                    "Un parent isolé ne peut pas être marié ou pacsé"
            );
        }

        if (personneSeule && revenuNetDeclarant2 > 0) {
            throw new IllegalArgumentException(
                    "Un célibataire, un divorcé ou un veuf ne peut pas " +
                            "avoir de revenu pour le déclarant 2"
            );
        }
    }
}
