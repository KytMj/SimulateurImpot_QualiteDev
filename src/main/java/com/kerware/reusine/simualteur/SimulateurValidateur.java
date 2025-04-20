package com.kerware.reusine.simualteur;

import com.kerware.simulateur.SituationFamiliale;

public class SimulateurValidateur {

    public static void validiteParametresCalCulImpot(
            int revenuNetDeclarant1,
            int revenuNetDeclarant2,
            SituationFamiliale situationFamiliale,
            int nombreEnfants,
            int nombreEnfantsHandicapes,
            boolean parentIsole
    ) throws IllegalArgumentException {

        VerificationValeurNonNegative(revenuNetDeclarant1, "Le revenu net ne peut pas être négatif");
        VerificationValeurNonNegative(revenuNetDeclarant2, "Le revenu net ne peut pas être négatif");
        VerificationValeurNonNegative(nombreEnfants, "Le nombre d'enfants ne peut pas être négatif");
        VerificationValeurNonNegative(nombreEnfantsHandicapes, "Le nombre d'enfants handicapés ne peut pas être négatif");

        VerificationNombreEnfants(nombreEnfants, nombreEnfantsHandicapes);

        boolean personneSeule = EstSeule(situationFamiliale);
        VerificationSituationFamiliale(situationFamiliale, personneSeule, parentIsole, revenuNetDeclarant2);
    }

    private static boolean EstSeule(SituationFamiliale situationFamiliale){
        return (situationFamiliale == SituationFamiliale.CELIBATAIRE || situationFamiliale == SituationFamiliale.DIVORCE || situationFamiliale == SituationFamiliale.VEUF);
    }

    private static void VerificationNombreEnfants(int nombreEnfants, int nombreEnfantsHandicapes){
        if (nombreEnfantsHandicapes > nombreEnfants) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut pas être supérieur au nombre d'enfants");
        }

        if (nombreEnfants > 7) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être supérieur à 7");
        }
    }

    private static void VerificationValeurNonNegative(int valeur, String message) throws IllegalArgumentException{
        if(valeur < 0){
            throw new IllegalArgumentException(message);
        }
    }

    private static void VerificationSituationFamiliale(
            SituationFamiliale situationFamiliale,
            boolean personneSeule,
            boolean parentIsole,
            int revenuNetDeclarant2
    ) throws IllegalArgumentException {
        if (situationFamiliale == null) {
            throw new IllegalArgumentException("La situation familiale ne peut pas être null");
        }

        if (parentIsole && (situationFamiliale == SituationFamiliale.MARIE || situationFamiliale == SituationFamiliale.PACSE)) {
            throw new IllegalArgumentException("Un parent isolé ne peut pas être marié ou pacsé");
        }

        if (personneSeule && revenuNetDeclarant2 > 0) {
            throw new IllegalArgumentException("Un célibataire, un divorcé ou un veuf ne peut pas avoir de revenu pour le déclarant 2");
        }
    }
}
